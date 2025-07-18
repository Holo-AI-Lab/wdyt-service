package ai.holo.wdyt.wardrobe.service;

import ai.holo.wdyt.askai.service.AiFeedbackService;
import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.chatgpt.ChatGptService;
import ai.holo.wdyt.common.chatgpt.ChatGptText2ImageService;
import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.service.UserService;
import ai.holo.wdyt.wardrobe.model.dto.DraftWardrobeItemsDto;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;
import ai.holo.wdyt.wardrobe.service.prompt.ImageWardrobeItemDetectionPrompt;
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

@Service
public class WardrobeItemAutoExtractService {
    private final ChatGptService chatGptService;
    private final ChatGptText2ImageService chatGptText2ImageService;
    private final S3Service s3Service;
    private final UserService userService;

    public WardrobeItemAutoExtractService(ChatGptService chatGptService, ChatGptText2ImageService chatGptText2ImageService, S3Service s3Service, UserService userService) {
        this.chatGptService = chatGptService;
        this.chatGptText2ImageService = chatGptText2ImageService;
        this.s3Service = s3Service;
        this.userService = userService;
    }

    public List<DraftWardrobeItemsDto> extractWardrobeItems(String imageUrl) {
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
        UserDto userInfo = userService.getUserInfo();

        return executeImageGenerationInParallel(wardrobeItemImageGenerationPrompts, userInfo);
    }

    private List<DraftWardrobeItemsDto> executeImageGenerationInParallel(List<WardrobeItemImageGenerationPrompt> wardrobeItemImageGenerationPrompts, UserDto userInfo) {
        ExecutorService executor = Executors.newFixedThreadPool(8); // adjust pool size based on your system
        List<CompletableFuture<DraftWardrobeItemsDto>> futures = wardrobeItemImageGenerationPrompts.stream()
                .map(prompt -> CompletableFuture.supplyAsync(() -> {
                    byte[] image = chatGptText2ImageService.generateImage(prompt.text2ImagePrompt());
                    InputStream imageStream = new ByteArrayInputStream(image);
                    long currentTimeMillis = System.currentTimeMillis();
                    String path = String.format("%d/%s/%d/wardrobe_item_%d.png",
                            userInfo.id(), "wardrobe", currentTimeMillis, currentTimeMillis);
                    String savedImagePath = s3Service.saveImage(imageStream, path);
                    return new DraftWardrobeItemsDto(
                            prompt.item.name(),
                            prompt.item.content(),
                            prompt.item.label(),
                            prompt.item.color(),
                            prompt.item.colorCode(),
                            prompt.item.season(),
                            s3Service.getFileS3Url(savedImagePath)
                    );
                }, executor))
                .toList();

        List<DraftWardrobeItemsDto> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        executor.shutdown();
        return results;
    }

    private List<WardrobeItemImageGenerationPrompt> generateImageGenerationPrompts(List<DetectedWardrobeItemResponse> detectedWardrobeItemResponses) {
        String shoesPrompt = "Surreal-style product image, single item display, centered placement, laid flat, side view, well-lit studio shot, no distortion. The background should be pure white, with no other decorations or objects besides the product. There should be blank space around the product. No people should appear in the image.";
        String genericPrompt = "Surreal-style product image, single item display, centered placement, laid flat, front-facing, well-lit studio shot, no surreal elements, no distortion. The background should be pure white, with no other decorations or objects besides the product. There should be blank space around the product. No people should appear in the image.";
        return detectedWardrobeItemResponses.stream().map(item -> {
            String colorPrompt = item.isColorStripesIntersecting() ? String.format("Multiple colors are staggered in stripes, a %s", item.color()) : String.format("a %s", item.color());
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
            String color,
            List<String> colorCode,
            String season,
            String colorStripesIntersecting
    ) {
        public boolean isColorStripesIntersecting() {
            return "Yes".equalsIgnoreCase(colorStripesIntersecting);
        }
    }

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
