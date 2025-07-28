package ai.holo.wdyt.wardrobe.service;

import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.askai.repository.AiFeedbackRepository;
import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.chatgpt.ChatGptService;
import ai.holo.wdyt.common.chatgpt.ChatGptText2ImageService;
import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.subscription.service.UserCreditService;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import ai.holo.wdyt.wardrobe.model.dto.DraftWardrobeItemDto;
import ai.holo.wdyt.wardrobe.model.entity.*;
import ai.holo.wdyt.wardrobe.repository.DraftWardrobeItemRepository;
import ai.holo.wdyt.wardrobe.service.prompt.WardrobeItemAutomaticExtractionPrompt;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Service
public class WardrobeItemAutoExtractService {
    private final ChatGptService chatGptService;
    private final ChatGptText2ImageService chatGptText2ImageService;
    private final S3Service s3Service;
    private final UserService userService;
    private final DraftWardrobeItemRepository draftWardrobeItemRepository;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final UserCreditService userCreditService;

    private static final int MAX_CONCURRENT_REQUESTS = 3; // Reduce to avoid 429
    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 1000;

    public WardrobeItemAutoExtractService(ChatGptService chatGptService, ChatGptText2ImageService chatGptText2ImageService, S3Service s3Service,
                                          UserService userService, DraftWardrobeItemRepository draftWardrobeItemRepository, AiFeedbackRepository aiFeedbackRepository, UserCreditService userCreditService) {
        this.chatGptService = chatGptService;
        this.chatGptText2ImageService = chatGptText2ImageService;
        this.s3Service = s3Service;
        this.userService = userService;
        this.draftWardrobeItemRepository = draftWardrobeItemRepository;
        this.aiFeedbackRepository = aiFeedbackRepository;
        this.userCreditService = userCreditService;
    }

    public List<DraftWardrobeItemDto> extractWardrobeItems(Long aiFeedbackId) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(aiFeedbackId).orElseThrow(NotFoundException::new);
        User user = userService.getUser();
        if (!aiFeedback.getUserId().equals(user.getId())) {
            throw new BadRequestException("You can only extract wardrobe items from your own feedback.");
        }
        List<DraftWardrobeItem> existingDraftWardrobeItems = draftWardrobeItemRepository.findByAiFeedbackId(aiFeedbackId);
        if (existingDraftWardrobeItems.isEmpty()) {
            userCreditService.checkEnoughCreditsExisting(user, UserCreditService.WARDROBE_AUTO_EXTRACTION_COST);
            return extractItems(user, s3Service.getFileS3Url(aiFeedback.getExtractedImagePath()), aiFeedback.getId());
        }
        return existingDraftWardrobeItems.stream().map(draftItem -> new DraftWardrobeItemDto(draftItem, s3Service.getFileS3Url(draftItem.getImagePath())))
                .toList();
    }

    private List<DraftWardrobeItemDto> extractItems(User user, String imageUrl, Long aiFeedbackId) {
        String systemPrompt = new WardrobeItemAutomaticExtractionPrompt().getSystemPrompt();

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

        List<DraftWardrobeItem> draftWardrobeItems = executeImageGenerationInParallel(wardrobeItemImageGenerationPrompts, user, aiFeedbackId);
        List<DraftWardrobeItem> savedDraftItems = draftWardrobeItemRepository.saveAll(draftWardrobeItems);
        return savedDraftItems.stream()
                .map(draftItem -> new DraftWardrobeItemDto(draftItem, s3Service.getFileS3Url(draftItem.getImagePath())))
                .toList();
    }

    private List<DraftWardrobeItem> executeImageGenerationInParallel(
            List<WardrobeItemImageGenerationPrompt> prompts, User user, Long aiFeedbackId) {

        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);
        Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

        List<CompletableFuture<DraftWardrobeItem>> futures = prompts.stream()
                .map(prompt -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire(); // limit concurrent requests
                        return generateImageWithRetry(prompt, user, aiFeedbackId);
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

    private DraftWardrobeItem generateImageWithRetry(WardrobeItemImageGenerationPrompt prompt, User user, Long aiFeedbackId) {
        int attempt = 0;
        while (true) {
            try {
                byte[] image = chatGptText2ImageService.generateImage(prompt.text2ImagePrompt());
                InputStream imageStream = new ByteArrayInputStream(image);
                long now = System.currentTimeMillis();
                String path = String.format("%d/wardrobe/%d/wardrobe_item_%d.png",
                        user.getId(), now, now);
                String savedImagePath = s3Service.saveImage(imageStream, path);

                return new DraftWardrobeItem(
                        user.getId(),
                        aiFeedbackId,
                        prompt.item.name(),
                        prompt.item.content(),
                        WardrobeItemCategory.fromValue(prompt.item.label()),
                        prompt.item.subCategories.stream().map(s -> new SubCategory(s.name())).toList(),
                        prompt.item.colors.stream()
                                .map(c -> new Color(c.name(), c.code()))
                                .toList(),
                        prompt.item.seasons.stream().map(s -> new Season(s.name())).toList(),
                        savedImagePath,
                        WardrobeItemExtractionType.AUTOMATIC,
                        prompt.item.tags.stream().map(t -> new DraftItemTag(t.name())).toList()
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
        String shoesPrompt = "Surreal-style product image, single item display, centered placement, laid flat, side view, well-lit studio shot, no distortion. No background, no people, with no other decorations or objects besides the item.";
        String genericPrompt = "Surreal-style product image, single item display, centered placement, laid flat, front-facing, well-lit studio shot, no surreal elements, no distortion. No background, no people, with no other decorations or objects besides the item.";
        return detectedWardrobeItemResponses.stream().map(item -> {
            String colorPrompt = item.colorStripesIntersecting() ? String.format("Multiple colors are staggered in stripes, a %s", item.colors) : String.format("a %s", item.colors());
            String subCategories = item.subCategories().stream().map(DetectedWardrobeItemSubCategory::name).collect(Collectors.joining(","));
            if (WardrobeItemCategory.FOOTWEAR.getDisplayName().equals(item.label())) {
                return new WardrobeItemImageGenerationPrompt(item, String.format("%s %s %s %s", shoesPrompt, colorPrompt, subCategories, item.content()));
            } else {
                return new WardrobeItemImageGenerationPrompt(item, String.format("%s %s %s %s", genericPrompt, colorPrompt, subCategories, item.content()));
            }
        }).toList();
    }

    private List<DetectedWardrobeItemResponse> parseResponseContent(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(
                    content,
                    new TypeReference<List<DetectedWardrobeItemResponse>>() {}
            );
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
            List<DetectedWardrobeItemSubCategory> subCategories,
            List<DetectedWardrobeItemColor> colors,
            List<DetectedWardrobeItemSeason> seasons,
            boolean colorStripesIntersecting,
            List<DetectedWardrobeTag> tags
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetectedWardrobeItemColor(String name, String code) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetectedWardrobeItemSeason(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetectedWardrobeItemSubCategory(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetectedWardrobeTag(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponsePayload(String id, List<ResponseAssistantMessage> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponseAssistantMessage(ResponseMessage message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponseMessage(String content) {
    }

    public record WardrobeItemImageGenerationPrompt(DetectedWardrobeItemResponse item, String text2ImagePrompt) {}
}
