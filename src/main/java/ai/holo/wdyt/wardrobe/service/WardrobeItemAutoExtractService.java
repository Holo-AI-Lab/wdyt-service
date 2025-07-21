package ai.holo.wdyt.wardrobe.service;

import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.askai.repository.AiFeedbackRepository;
import ai.holo.wdyt.askai.service.AiFeedbackService;
import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.chatgpt.ChatGptService;
import ai.holo.wdyt.common.chatgpt.ChatGptText2ImageService;
import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.service.UserService;
import ai.holo.wdyt.wardrobe.model.dto.DraftWardrobeItemDto;
import ai.holo.wdyt.wardrobe.model.entity.Color;
import ai.holo.wdyt.wardrobe.model.entity.DraftWardrobeItem;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;
import ai.holo.wdyt.wardrobe.repository.DraftWardrobeItemRepository;
import ai.holo.wdyt.wardrobe.service.prompt.ImageWardrobeItemDetectionPrompt;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.*;

@Service
public class WardrobeItemAutoExtractService {
    private final ChatGptService chatGptService;
    private final ChatGptText2ImageService chatGptText2ImageService;
    private final S3Service s3Service;
    private final UserService userService;
    private final DraftWardrobeItemRepository draftWardrobeItemRepository;
    private final AiFeedbackRepository aiFeedbackRepository;

    private static final int MAX_CONCURRENT_REQUESTS = 3; // Reduce to avoid 429
    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 1000;

    public WardrobeItemAutoExtractService(ChatGptService chatGptService, ChatGptText2ImageService chatGptText2ImageService, S3Service s3Service,
                                          UserService userService, DraftWardrobeItemRepository draftWardrobeItemRepository, AiFeedbackRepository aiFeedbackRepository) {
        this.chatGptService = chatGptService;
        this.chatGptText2ImageService = chatGptText2ImageService;
        this.s3Service = s3Service;
        this.userService = userService;
        this.draftWardrobeItemRepository = draftWardrobeItemRepository;
        this.aiFeedbackRepository = aiFeedbackRepository;
    }

    public List<DraftWardrobeItemDto> extractWardrobeItems(Long aiFeedbackId) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(aiFeedbackId).orElseThrow(NotFoundException::new);
        UserDto userInfo = userService.getUserInfo();
        if (!aiFeedback.getUserId().equals(userInfo.id())) {
            throw new BadRequestException("You can only extract wardrobe items from your own feedback.");
        }
        List<DraftWardrobeItem> existingDraftWardrobeItems = draftWardrobeItemRepository.findByAiFeedbackId(aiFeedbackId);
        if (existingDraftWardrobeItems.isEmpty()) {
            return extractItems(userInfo, s3Service.getFileS3Url(aiFeedback.getExtractedImagePath()), aiFeedback.getId());
        }
        return existingDraftWardrobeItems.stream().map(draftItem -> new DraftWardrobeItemDto(draftItem, s3Service.getFileS3Url(draftItem.getImagePath())))
                .toList();
    }

    private List<DraftWardrobeItemDto> extractItems(UserDto userInfo, String imageUrl, Long aiFeedbackId) {
        String systemPrompt = new ImageWardrobeItemDetectionPrompt().getSystemPrompt();

        List<ChatGptService.Message> messages = List.of(
                new ChatGptService.Message("system", List.of(new ChatGptService.MessageContent("text", systemPrompt, null))),
                new ChatGptService.Message("user", List.of(new ChatGptService.MessageContent("image_url", null, new ChatGptService.ImageAttachment(imageUrl)))));

        String gptResponse = chatGptService.sendPrompt(messages);
        if (gptResponse == null || gptResponse.isEmpty()) {
            throw new BadRequestException("Failed to extract wardrobe items from the image.");
        }
        String content = getGptContent(gptResponse);
        List<DetectedWardrobeItemResponse> detectedWardrobeItemResponses = parseResponseContent(content);
        List<WardrobeItemImageGenerationPrompt> wardrobeItemImageGenerationPrompts = generateImageGenerationPrompts(detectedWardrobeItemResponses);

        List<DraftWardrobeItem> draftWardrobeItems = executeImageGenerationInParallel(wardrobeItemImageGenerationPrompts, userInfo, aiFeedbackId);
        List<DraftWardrobeItem> savedDraftItems = draftWardrobeItemRepository.saveAll(draftWardrobeItems);
        return savedDraftItems.stream()
                .map(draftItem -> new DraftWardrobeItemDto(draftItem, s3Service.getFileS3Url(draftItem.getImagePath())))
                .toList();
    }

    private List<DraftWardrobeItem> executeImageGenerationInParallel(
            List<WardrobeItemImageGenerationPrompt> prompts, UserDto userInfo, Long aiFeedbackId) {

        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);
        Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

        List<CompletableFuture<DraftWardrobeItem>> futures = prompts.stream()
                .map(prompt -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire(); // limit concurrent requests
                        return generateImageWithRetry(prompt, userInfo, aiFeedbackId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during semaphore acquire", e);
                    } finally {
                        semaphore.release();
                    }
                }, executor))
                .toList();

        List<DraftWardrobeItem> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        executor.shutdown();
        return results;
    }

    private DraftWardrobeItem generateImageWithRetry(WardrobeItemImageGenerationPrompt prompt, UserDto userInfo, Long aiFeedbackId) {
        int attempt = 0;
        while (true) {
            try {
                byte[] image = chatGptText2ImageService.generateImage(prompt.text2ImagePrompt());
                InputStream imageStream = new ByteArrayInputStream(image);
                long now = System.currentTimeMillis();
                String path = String.format("%d/wardrobe/%d/wardrobe_item_%d.png",
                        userInfo.id(), now, now);
                String savedImagePath = s3Service.saveImage(imageStream, path);

                return new DraftWardrobeItem(
                        userInfo.id(),
                        aiFeedbackId,
                        prompt.item.name(),
                        prompt.item.content(),
                        WardrobeItemCategory.fromValue(prompt.item.label()),
                        prompt.item.colors.stream()
                                .map(c -> new Color(c.name(), c.code()))
                                .toList(),
                        prompt.item.season(),
                        savedImagePath
                );
            } catch (RuntimeException ex) {
                if (isRateLimitError(ex) && attempt < MAX_RETRIES) {
                    long backoff = BASE_BACKOFF_MS * (1L << attempt); // exponential backoff
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during backoff", ie);
                    }
                    attempt++;
                } else {
                    throw ex;
                }
            }
        }
    }

    private boolean isRateLimitError(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof RuntimeException) {
                if (cause.getMessage() != null && cause.getMessage().contains("429")) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
    private List<WardrobeItemImageGenerationPrompt> generateImageGenerationPrompts(List<DetectedWardrobeItemResponse> detectedWardrobeItemResponses) {
        String shoesPrompt = "Surreal-style product image, single item display, centered placement, laid flat, side view, well-lit studio shot, no distortion. The background should be pure white, with no other decorations or objects besides the product. There should be blank space around the product. No people should appear in the image.";
        String genericPrompt = "Surreal-style product image, single item display, centered placement, laid flat, front-facing, well-lit studio shot, no surreal elements, no distortion. The background should be pure white, with no other decorations or objects besides the product. There should be blank space around the product. No people should appear in the image.";
        return detectedWardrobeItemResponses.stream().map(item -> {
            String colorPrompt = item.colorStripesIntersecting() ? String.format("Multiple colors are staggered in stripes, a %s", item.colors) : String.format("a %s", item.colors());
            if (WardrobeItemCategory.FOOTWEAR.getDisplayName().equals(item.label())) {
                return new WardrobeItemImageGenerationPrompt(item, String.format("%s %s %s %s", shoesPrompt, colorPrompt, item.cloLabel(), item.content()));
            } else {
                return new WardrobeItemImageGenerationPrompt(item, String.format("%s %s %s %s", genericPrompt, colorPrompt, item.cloLabel(), item.content()));
            }
        }).toList();
    }

    private List<DetectedWardrobeItemResponse> parseResponseContent(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<DetectedWardrobeItemResponse> items = mapper.readValue(
                    content,
                    new TypeReference<List<DetectedWardrobeItemResponse>>() {}
            );
            return items;
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to parse the response from AI: " + e.getMessage());
        }
    }

    private String getGptContent(String gptResponse) {
        try {
            String content = new ObjectMapper().readValue(gptResponse, ResponsePayload.class)
                    .choices().stream()
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("No response from AI."))
                    .message().content();
            return content.replaceFirst("^```json\\s*", "")
                    .replaceFirst("```\\s*$", "").trim();

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetectedWardrobeItemResponse(
            String name,
            String content,
            String label,
            String cloLabel,
            List<DetectedWardrobeItemColor> colors,
            String season,
            boolean colorStripesIntersecting
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetectedWardrobeItemColor(String name, String code) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponsePayload(String id, List<AiFeedbackService.AIResponseAssistantMessage> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponseAssistantMessage(AiFeedbackService.AIMessage message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponseMessage(String content) {
    }

    public record WardrobeItemImageGenerationPrompt(DetectedWardrobeItemResponse item, String text2ImagePrompt) {}
}
