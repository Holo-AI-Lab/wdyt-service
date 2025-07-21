package ai.holo.wdyt.wardrobe.service;

import ai.holo.wdyt.askai.service.PhotoroomBgExtractionService;
import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.chatgpt.ChatGptService;
import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.service.UserService;
import ai.holo.wdyt.wardrobe.model.dto.DraftWardrobeItemDto;
import ai.holo.wdyt.wardrobe.model.dto.WardrobeManualExtractDto;
import ai.holo.wdyt.wardrobe.model.dto.WardrobeManualExtractRequestDataDto;
import ai.holo.wdyt.wardrobe.model.entity.Color;
import ai.holo.wdyt.wardrobe.model.entity.DraftWardrobeItem;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemExtractionType;
import ai.holo.wdyt.wardrobe.repository.DraftWardrobeItemRepository;
import ai.holo.wdyt.wardrobe.service.prompt.WardrobeItemManualExtractionPrompt;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
public class WardrobeItemManualExtractService {
    private final ChatGptService chatGptService;
    private final S3Service s3Service;
    private final UserService userService;
    private final DraftWardrobeItemRepository draftWardrobeItemRepository;
    private final PhotoroomBgExtractionService photoroomBgExtractionService;

    public WardrobeItemManualExtractService(ChatGptService chatGptService, S3Service s3Service,
                                            UserService userService, DraftWardrobeItemRepository draftWardrobeItemRepository,
                                            PhotoroomBgExtractionService photoroomBgExtractionService) {
        this.chatGptService = chatGptService;
        this.s3Service = s3Service;
        this.userService = userService;
        this.draftWardrobeItemRepository = draftWardrobeItemRepository;
        this.photoroomBgExtractionService = photoroomBgExtractionService;
    }

    public WardrobeManualExtractDto validateAndParseManualExtractDto(byte[] imageBytes, String data) {
        try {
            if (imageBytes == null || imageBytes.length == 0) {
                throw new BadRequestException("Image is required for manual extraction");
            }

            WardrobeManualExtractRequestDataDto wardrobeManualExtractRequestDataDto = new ObjectMapper().readValue(data, WardrobeManualExtractRequestDataDto.class);
            return new WardrobeManualExtractDto(wardrobeManualExtractRequestDataDto.bgExtracted(), imageBytes);

        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid manual extract request");
        }
    }

    public String prepareImageForManualExtraction(WardrobeManualExtractDto wardrobeManualExtractRequestDto) {
        UserDto userInfo = userService.getUserInfo();
        long now = System.currentTimeMillis();
        String path = String.format("%d/wardrobe/%d/raw_manual_wardrobe_item_%d.png",
                userInfo.id(), now, now);
        String rawImagePath = s3Service.saveImage(new ByteArrayInputStream(wardrobeManualExtractRequestDto.image()), path);
        if (wardrobeManualExtractRequestDto.bgExtracted()) {
            return rawImagePath;
        }
        InputStream bgExtractedImage = photoroomBgExtractionService.extractBackground(wardrobeManualExtractRequestDto.image(), rawImagePath);
        String bgExtractedImagePath = String.format("%d/wardrobe/%d/bg_extracted_manual_wardrobe_item_%d.png",
                userInfo.id(), now, now);
        return s3Service.saveImage(bgExtractedImage, bgExtractedImagePath);
    }

    public DraftWardrobeItemDto extractWardrobeItems(String imagePath) {
        String systemPrompt = new WardrobeItemManualExtractionPrompt().getSystemPrompt();
        String imageUrl = s3Service.getFileS3Url(imagePath);
        List<ChatGptService.Message> messages = List.of(
                new ChatGptService.Message("system", List.of(new ChatGptService.MessageContent("text", systemPrompt, null))),
                new ChatGptService.Message("user", List.of(new ChatGptService.MessageContent("image_url", null, new ChatGptService.ImageAttachment(imageUrl)))));

        String response = chatGptService.sendPrompt(messages);
        if (response == null || response.isEmpty()) {
            throw new BadRequestException("Failed to extract wardrobe items from the image");
        }
        String content = getGptContent(response);
        WardrobeItemManualExtractResponse wardrobeItemManualExtractResponse = extractManualItemsResponse(content, imageUrl);
        UserDto userInfo = userService.getUserInfo();
        DraftWardrobeItem draftWardrobeItem = new DraftWardrobeItem(userInfo.id(), null, wardrobeItemManualExtractResponse.item.name(), null,
                WardrobeItemCategory.fromValue(wardrobeItemManualExtractResponse.item.label()), wardrobeItemManualExtractResponse.item.subLabel(),
                wardrobeItemManualExtractResponse.item.colors().stream().map(color -> new Color(color.name(), color.code())).toList(),
                wardrobeItemManualExtractResponse.item.season(), imagePath, WardrobeItemExtractionType.MANUAL);
        DraftWardrobeItem savedWardrobeItem = draftWardrobeItemRepository.save(draftWardrobeItem);
        return new DraftWardrobeItemDto(savedWardrobeItem, imageUrl);
    }

    private WardrobeItemManualExtractResponse extractManualItemsResponse(String content, String imageUrl) {
        WardrobeItemManualExtractResponse wardrobeItemManualExtractResponse = null;
        try {
            wardrobeItemManualExtractResponse = new ObjectMapper().readValue(content, WardrobeItemManualExtractResponse.class);
            if (!wardrobeItemManualExtractResponse.valid()) {
                log.warn("Wardrobe item extraction failed for {} reason: {}", imageUrl, wardrobeItemManualExtractResponse.reason());
                throw new BadRequestException("Wardrobe item extraction failed: " + wardrobeItemManualExtractResponse.reason());
            }
            return wardrobeItemManualExtractResponse;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getGptContent(String gptResponse) {
        try {
            String content = new ObjectMapper().readValue(gptResponse, WardrobeItemManualExtractService.ResponsePayload.class)
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
    public record WardrobeItemManualExtractResponse (boolean valid,
                                                     String reason,
                                                     DetectedWardrobeItemResponse item) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetectedWardrobeItemResponse(
            String name,
            String label,
            String subLabel,
            List<DetectedWardrobeItemColor> colors,
            String season
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetectedWardrobeItemColor(String name, String code) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponsePayload(String id, List<ResponseAssistantMessage> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponseAssistantMessage(ResponseMessage message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponseMessage(String content) {
    }
}

