package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.askai.model.entity.AiFeedbackOrder;
import ai.holo.wdyt.askai.model.entity.ChatGptPrompt;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.askai.repository.AiFeedbackOrderRepository;
import ai.holo.wdyt.askai.repository.AiFeedbackRepository;
import ai.holo.wdyt.common.JsonUtils;
import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.common.exception.InvalidImageException;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Slf4j
public class AiFeedbackService {
    public static final String BODY_JSON_FORMATTING_PROMPT = " We would like the response in this format: {\"outfit_style\":\"string\",\"style_match\":\"string\",\"occasion_fit\":\"string\",\"trend_alert\":\"string\",\"outfit_details\":[{\"item\":\"string\",\"color\":\"string\",\"description\":\"string\"}],\"color_preference\":{\"primary\":\"string\",\"secondary\":\"string\"},\"enhancement_recommendations\":[\"string\"],\"hair_advice\":\"string\",\"coordinate_recommendations\":{\"outfit\":[{\"x\":int,\"y\":int}],\"enhancements\":[{\"x\":int,\"y\":int}]},\"summary\":\"string\",\"compliment\":\"string\"}";
    public static final String HEAD_JSON_FORMATTING_PROMPT = " We would like the response in this format: {\"head_style\":\"string\",\"style_face_fit\":\"string\",\"occasion_fit\":\"string\",\"trend_alert\":\"string\",\"detailed_elements\":[{\"item\":\"string\",\"description\":\"string\",\"color\":\"string\"}],\"color_preference\":{\"primary\":\"string\",\"secondary\":\"string\"},\"enhancement_recommendations\":[\"string\"],\"hair_advice\":\"string\",\"coordinate_recommendations\":{\"elements\":[{\"x\":int,\"y\":int}],\"enhancements\":[{\"x\":int,\"y\":int}]},\"summary\":\"string\",\"compliment\":\"string\"}";
    public static final int MAX_RETRY_COUNT = 3;
    private final ChatGptService chatGptService;
    private final S3Service s3Service;
    private final BackgroundExtractionService backgroundExtractionService;
    private final ImageClassificationService imageClassificationService;
    private final UserService userService;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final String s3Endpoint;
    private final int topListCount;
    private final IpGeoLocationService ipGeoLocationService;
    private final PromptService promptService;
    private final AiFeedbackOrderRepository aiFeedbackOrderRepository;

    public AiFeedbackService(ChatGptService chatGptService, S3Service s3Service,
                             BackgroundExtractionService backgroundExtractionService,
                             ImageClassificationService imageClassificationService,
                             UserService userService,
                             AiFeedbackRepository aiFeedbackRepository,
                             @Value("${aws.s3.endpoint}") String s3Endpoint,
                             @Value("${configuration.topListCount}") int topListCount,
                             IpGeoLocationService ipGeoLocationService, PromptService promptService,
                             AiFeedbackOrderRepository aiFeedbackOrderRepository) {
        this.chatGptService = chatGptService;
        this.s3Service = s3Service;
        this.backgroundExtractionService = backgroundExtractionService;
        this.imageClassificationService = imageClassificationService;
        this.userService = userService;
        this.aiFeedbackRepository = aiFeedbackRepository;
        this.s3Endpoint = s3Endpoint;
        this.topListCount = topListCount;
        this.ipGeoLocationService = ipGeoLocationService;
        this.promptService = promptService;
        this.aiFeedbackOrderRepository = aiFeedbackOrderRepository;
    }

    public AiFeedback executeGptCall(byte[] image, String clientIpAddress, ZonedDateTime clientTime, UserDto userInfo) {
        long currentTimeMillis = System.currentTimeMillis();
        // Save Raw image
        String rawImagePath = saveRawImageOnS3(new ByteArrayInputStream(image), userInfo, currentTimeMillis);

        // Classify image
        ImageType imageType = imageClassificationService.classifyImage(image);
        if (imageType == ImageType.OTHER) {
            throw new BadRequestException("Provided image is not appropriate for AI processing.");
        }

        // Extract background and save extracted image
        InputStream extractedImage = backgroundExtractionService.extractBackground(image, rawImagePath);
        String extractedImagePath = saveExtractedImageOnS3(userInfo, currentTimeMillis, extractedImage);

        // Get location by IP
        String locationByIp = ipGeoLocationService.getLocationByIp(clientIpAddress);

        // Send prompt with image to ChatGPT
        ChatGptPrompt prompt = promptService.getPrompt(imageType);
        String promptText = getPromptText(prompt, locationByIp, clientTime);

        // Call ChatGPT with retries
        String gptResponse = sendPromptWithRetries(getFileS3Url(extractedImagePath), promptText, imageType);

        AiSubmissionOrder orders = getOrder(userInfo);
        // Save AI response
        return new AiFeedback(userInfo.id(), prompt.getId(),
                gptResponse, rawImagePath, imageType, extractedImagePath, orders.topListOrder(), orders.order(), locationByIp);
    }

    private String sendPromptWithRetries(String extractedImagePath, String promptText, ImageType imageType) {
        int retries = MAX_RETRY_COUNT;
        for (int i = 0; i < retries; i++) {
            String gptResponse = null;
            try {
                // Attempt to send the prompt and extract the response
                gptResponse = chatGptService.sendPromptWithImage(extractedImagePath, promptText);
                extractResponse(gptResponse, imageType);
                return gptResponse; // Return the response if successful
            } catch (RuntimeException e) {
                if (i == retries - 1) { // If it's the last attempt, rethrow the exception
                    log.error("Failed to get response from AI service. Response: {}", gptResponse);
                    throw new InvalidImageException();
                }
                // Log the retry attempt
                log.warn("Retrying sendPromptWithImage due to failure: " + e.getMessage());

                // Sleep for 500ms before retrying
                sleep500Millis();
            }
        }
        throw new IllegalStateException("Unexpected error in retry logic"); // Should never reach here
    }

    private void sleep500Millis() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new RuntimeException("Retry was interrupted", interruptedException);
        }
    }

    @Transactional
    public AiFeedbackDetailedDto saveAiResponse(AiFeedback aiFeedback, UserDto userInfo) {
        AiFeedback savedAiFeedback = aiFeedbackRepository.save(aiFeedback);

        Pair<OutfitAnalysis, HeadStyleAnalysis> analysis = extractResponse(savedAiFeedback.getResponse(), savedAiFeedback.getImageType());
        return new AiFeedbackDetailedDto(savedAiFeedback, analysis.getLeft(), analysis.getRight(),
                getFileS3Url(savedAiFeedback.getExtractedImagePath()), userInfo);
    }


    private AiSubmissionOrder getOrder(UserDto user) {
        Integer topListOrder = null;
        Integer order = null;
        int itemsInTopList = aiFeedbackRepository.countByUserIdAndTopListOrderIsNotNull(user.id());
        if (itemsInTopList < topListCount) {
            topListOrder = itemsInTopList + 1;
        }
        // if item is not in top list, increment order
        if (topListOrder == null) {
            AiFeedbackOrder aiFeedbackOrder = aiFeedbackOrderRepository.findByUserId(user.id()).orElse(new AiFeedbackOrder(user.id()));
            order = aiFeedbackOrder.incrementOrder();
            aiFeedbackOrderRepository.save(aiFeedbackOrder);
        }
        return new AiSubmissionOrder(topListOrder, order);
    }

    private String getPromptText(ChatGptPrompt prompt, String location, ZonedDateTime date) {
        String formattedDate = date.toString();
        String promptText = prompt.getPrompt();
        String jsonFormattingSuffix = ImageType.BODY.equals(prompt.getImageType()) ? BODY_JSON_FORMATTING_PROMPT : HEAD_JSON_FORMATTING_PROMPT;
        promptText = promptText + jsonFormattingSuffix;

        String locationRegex = "\\$\\{local}";
        String dateRegex = "\\$\\{date}";

        // Replace all occurrences of ${location} and ${date}
        promptText = promptText.replaceAll(locationRegex, location);
        return promptText.replaceAll(dateRegex, formattedDate);

    }

    private Pair<OutfitAnalysis, HeadStyleAnalysis> extractResponse(String response, ImageType imageType) {
        try {
            AIResponsePayload aiResponsePayload = new ObjectMapper().readValue(response, AIResponsePayload.class);
            String rawContent = aiResponsePayload.choices().get(0).message().content();
            String content = JsonUtils.preprocessGptJson(rawContent);
            OutfitAnalysis outfitAnalysis = null;
            HeadStyleAnalysis headStyleAnalysis = null;
            if (ImageType.BODY.equals(imageType)) {
                outfitAnalysis = new ObjectMapper().readValue(content, OutfitAnalysis.class);
            } else {
                headStyleAnalysis = new ObjectMapper().readValue(content, HeadStyleAnalysis.class);
            }
            return new ImmutablePair<>(outfitAnalysis, headStyleAnalysis);
        } catch (JsonProcessingException e) {
            log.error("Failed to extract response from AI response", e);
            throw new RuntimeException(e);
        }
    }

    private String getFileS3Url(String path) {
        return String.format("%s/%s", s3Endpoint, path);
    }

    private String saveExtractedImageOnS3(UserDto user, long currentTimeMillis, InputStream extractedImage) {
        String path = String.format("%d/%d/extracted_%d.png", user.id(), currentTimeMillis, currentTimeMillis);
        s3Service.saveImage(extractedImage, path);
        return path;
    }

    private String saveRawImageOnS3(InputStream image, UserDto user, long currentTimeMillis) {
        String path = String.format("%d/%d/raw_%d.png", user.id(), currentTimeMillis, currentTimeMillis);
        s3Service.saveImage(image, path);
        return path;
    }

    @Transactional(readOnly = true)
    public Page<AiFeedbackDto> listAiFeedbacks(PageRequest of) {
        Sort sortBy = Sort.by(
                Sort.Order.by("topListOrder").with(Sort.Direction.DESC), // `topListOrder` prioritized
                Sort.Order.by("order").with(Sort.Direction.DESC)         // Then by `order`
        );

        PageRequest pageRequestWithSort = PageRequest.of(of.getPageNumber(), of.getPageSize(), sortBy);

        UserDto userInfo = userService.getUserInfo();
        return aiFeedbackRepository.findAllByUserId(userInfo.id(), pageRequestWithSort).map(aiFeedback ->
                new AiFeedbackDto(aiFeedback, getFileS3Url(aiFeedback.getExtractedImagePath()), userInfo));
    }

    @Transactional
    public void swapFeedbackOrders(SwapAiFeedbackDto swapAiFeedbackDto) {
        User user = userService.getUser();
        AiFeedback aiFeedback1 = aiFeedbackRepository.findByIdAndUserId(swapAiFeedbackDto.feedbackOneId(), user.getId())
                .orElseThrow(NotFoundException::new);
        AiFeedback aiFeedback2 = aiFeedbackRepository.findByIdAndUserId(swapAiFeedbackDto.feedbackTwoId(), user.getId())
                .orElseThrow(NotFoundException::new);

        Integer topListOrder1 = aiFeedback1.getTopListOrder();
        Integer order1 = aiFeedback1.getOrder();

        Integer topListOrder2 = aiFeedback2.getTopListOrder();
        Integer order2 = aiFeedback2.getOrder();

        aiFeedback1.setTopListOrder(topListOrder2);
        aiFeedback1.setOrder(order2);

        aiFeedback2.setTopListOrder(topListOrder1);
        aiFeedback2.setOrder(order1);

        aiFeedbackRepository.save(aiFeedback1);
        aiFeedbackRepository.save(aiFeedback2);
    }

    @Transactional
    public AiFeedbackDto likeStyle(LikeStyleDto likeStyleDto) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(likeStyleDto.id()).orElseThrow(NotFoundException::new);
        aiFeedback.setLikeStyle(likeStyleDto.like());
        AiFeedback savedFeedback = aiFeedbackRepository.save(aiFeedback);
        return new AiFeedbackDto(savedFeedback, getFileS3Url(savedFeedback.getExtractedImagePath()), userService.getUserInfo());
    }

    @Transactional
    public AiFeedbackDto likeAiResponse(LikeAiResponseDto likeAiResponseDto) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(likeAiResponseDto.id()).orElseThrow(NotFoundException::new);
        if (aiFeedback.getLikeAiResponse() != null) {
            throw new BadRequestException("Feedback already liked");
        }
        aiFeedback.setLikeAiResponse(likeAiResponseDto.like());
        AiFeedback savedFeedback = aiFeedbackRepository.save(aiFeedback);
        return new AiFeedbackDto(savedFeedback, getFileS3Url(savedFeedback.getExtractedImagePath()), userService.getUserInfo());
    }

    @Transactional(readOnly = true)
    public AiFeedbackDetailedDto getAiFeedback(Long id) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(id).orElseThrow(NotFoundException::new);
        User user = userService.getUser();
        if (!aiFeedback.getUserId().equals(user.getId())) {
            throw new NotFoundException();
        }
        Pair<OutfitAnalysis, HeadStyleAnalysis> analysis = extractResponse(aiFeedback.getResponse(), aiFeedback.getImageType());
        return new AiFeedbackDetailedDto(aiFeedback, analysis.getLeft(), analysis.getRight(),
                getFileS3Url(aiFeedback.getExtractedImagePath()), userService.getUserInfo());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AIResponsePayload (String id, List<AIResponseAssistantMessage> choices) {
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AIResponseAssistantMessage (AIMessage message) {
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AIMessage (String content) {
    }

    private record AiSubmissionOrder(Integer topListOrder, Integer order) {
    }
}
