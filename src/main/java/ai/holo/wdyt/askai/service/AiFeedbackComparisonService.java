package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.model.entity.*;
import ai.holo.wdyt.askai.model.event.AiFeedbackReceivedEvent;
import ai.holo.wdyt.askai.repository.AiFeedbackComparisonRepository;
import ai.holo.wdyt.askai.repository.AiFeedbackRepository;
import ai.holo.wdyt.askai.repository.ReportAiFeedbackRepository;
import ai.holo.wdyt.askai.service.aiprompt.ComparisonUserPrompt;
import ai.holo.wdyt.askai.service.aiprompt.SystemPrompt;
import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.event.service.CallSupplierWithRetryService;
import ai.holo.wdyt.common.event.service.EventPublisher;
import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.common.exception.InsufficientCreditException;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.common.json.JsonUtils;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.subscription.service.UserCreditService;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@Slf4j
public class AiFeedbackComparisonService {
    private final AiFeedbackRepository aiFeedbackRepository;
    private final UserService userService;
    private final AiFeedbackComparisonRepository aiFeedbackComparisonRepository;
    private final S3Service s3Service;
    private final AiFeedbackSearchService aiFeedbackSearchService;
    private final ChatGptService chatGptService;
    private final CallSupplierWithRetryService callSupplierWithRetryService;
    private final EventPublisher eventPublisher;
    private final ReportAiFeedbackRepository reportAiFeedbackRepository;

    public AiFeedbackComparisonService(AiFeedbackRepository aiFeedbackRepository,
                                       UserService userService,
                                       AiFeedbackComparisonRepository aiFeedbackComparisonRepository,
                                       S3Service s3Service,
                                       AiFeedbackSearchService aiFeedbackSearchService,
                                       ChatGptService chatGptService,
                                       CallSupplierWithRetryService callSupplierWithRetryService,
                                       EventPublisher eventPublisher, ReportAiFeedbackRepository reportAiFeedbackRepository) {
        this.aiFeedbackRepository = aiFeedbackRepository;
        this.userService = userService;
        this.aiFeedbackComparisonRepository = aiFeedbackComparisonRepository;
        this.s3Service = s3Service;
        this.aiFeedbackSearchService = aiFeedbackSearchService;
        this.chatGptService = chatGptService;
        this.callSupplierWithRetryService = callSupplierWithRetryService;
        this.eventPublisher = eventPublisher;
        this.reportAiFeedbackRepository = reportAiFeedbackRepository;
    }

    @Transactional
    public AiComparisonDetailedDto saveAiCompareResponse(AiComparisonSubmissionDto comparisonSubmissionDto, String promptName, String gptResponse,
                                                         AISubmissionImagesForComparison comparisonImages, LocationAndWeatherDto locationAndWeather) {

        AiFeedback aiFeedback1 = aiFeedbackRepository.findById(comparisonSubmissionDto.feedback1()).orElseThrow(NotFoundException::new);
        AiFeedback aiFeedback2 = aiFeedbackRepository.findById(comparisonSubmissionDto.feedback2()).orElseThrow(NotFoundException::new);

        User user = userService.getUser();
        ComparisonAnalysis analysis = extractResponseForComparison(gptResponse);

        AiComparisonFeedback aiComparisonFeedback = new AiComparisonFeedback(user.getId(), aiFeedback1.getId(),
                aiFeedback2.getId(), comparisonImages.image1().imageType(),
                comparisonImages.image1().extractedImagePath(), comparisonImages.image2().extractedImagePath(),
                analysis.winner());

        aiComparisonFeedback.addFeedbackEntry(new FeedbackEntry(UUID.randomUUID().toString(), user.getId(), promptName,
                gptResponse, locationAndWeather, LocalDateTime.now()));

        Map<String, List<String>> tags = analysis.getTags();
        // Override occasion tags with the ones coming from the user
        if (!CollectionUtils.isEmpty(comparisonSubmissionDto.occasions())) {
            tags.put(Taggable.OCCASION, comparisonSubmissionDto.occasions());
        }

        aiComparisonFeedback.updateTags(tags);
        AiComparisonFeedback savedAiFeedback = aiFeedbackComparisonRepository.save(aiComparisonFeedback);

        eventPublisher.publishEvent(new AiFeedbackReceivedEvent(savedAiFeedback.getId(), user.getId(), user.getId()));

        return generateComparisonAiFeedbackDto(savedAiFeedback);
    }

    private AiComparisonDetailedDto generateComparisonAiFeedbackDto(AiComparisonFeedback comparisonFeedback) {
        List<FeedbackEntryDto> feedbackEntryDtos = comparisonFeedback.getFeedbackEntries().stream().map(feedback -> {
            ComparisonAnalysis analysis = extractResponseForComparison(feedback.response());
            analysis = overrideUserOccasionIfProvided(comparisonFeedback, analysis);
            User aiUser = userService.getUserById(feedback.userId());
            return new FeedbackEntryDto(feedback, null, null, analysis, new UserDto(aiUser));
        }).toList();
        return new AiComparisonDetailedDto(comparisonFeedback, s3Service.getFileS3Url(comparisonFeedback.getImage1Path()),
                s3Service.getFileS3Url(comparisonFeedback.getImage2Path()), userService.getUserInfo(), feedbackEntryDtos);
    }

    private static ComparisonAnalysis overrideUserOccasionIfProvided(AiComparisonFeedback comparisonFeedback, ComparisonAnalysis analysis) {
        // Override occasion tags with the ones coming from the user
        if (!CollectionUtils.isEmpty(comparisonFeedback.getTags().get(Taggable.OCCASION))) {
            analysis = new ComparisonAnalysis(analysis, comparisonFeedback.getTags().get(Taggable.OCCASION));
        }
        return analysis;
    }

    private ComparisonAnalysis extractResponseForComparison(String response) {
        try {
            AiFeedbackService.AIResponsePayload aiResponsePayload = new ObjectMapper().readValue(response, AiFeedbackService.AIResponsePayload.class);
            String rawContent = aiResponsePayload.choices().get(0).message().content();
            String content = JsonUtils.preprocessGptJson(rawContent);
            return new ObjectMapper().readValue(content, ComparisonAnalysis.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to extract response from AI response", e);
            throw new RuntimeException(e);
        }
    }

    public void checkIfUserHasEnoughCredits() {
        boolean userHasEnoughCredits = userService.getUser().getCreditBalance() >= UserCreditService.AI_FEEDBACK_COST;
        if (!userHasEnoughCredits) {
            throw new InsufficientCreditException(userService.getUser().getId());
        }
    }

    public AISubmissionImagesForComparison getComparisonImages(AiComparisonSubmissionDto comparisonSubmissionDto) {
        AiFeedback aiFeedback1 = aiFeedbackRepository.findById(comparisonSubmissionDto.feedback1()).orElseThrow(NotFoundException::new);
        AIComparisonSubmissionImage image1 = new AIComparisonSubmissionImage(aiFeedback1.getImageType(), aiFeedback1.getRawImagePath(), aiFeedback1.getExtractedImagePath());

        AiFeedback aiFeedback2 = aiFeedbackRepository.findById(comparisonSubmissionDto.feedback2()).orElseThrow(NotFoundException::new);
        AIComparisonSubmissionImage image2 = new AIComparisonSubmissionImage(aiFeedback2.getImageType(), aiFeedback2.getRawImagePath(), aiFeedback2.getExtractedImagePath());

        if (!ImageType.BODY.equals(image1.imageType()) || !ImageType.BODY.equals(image2.imageType())) {
            throw new BadRequestException("AI comparison is only supported for body images.");
        }
        return new AISubmissionImagesForComparison(image1, image2);
    }

    public String getComparisonPrompt(AiComparisonSubmissionDto comparisonSubmissionDto, LocationAndWeatherDto locationAndWeather) {
        String occasion = CollectionUtils.isEmpty(comparisonSubmissionDto.occasions()) ? " " : comparisonSubmissionDto.occasions().get(0);
        String weather = locationAndWeather.weather() != null ? locationAndWeather.weather().condition() : "unknown";
        // TODO : will arrange all parameters and use builder based on new prompt.
        ComparisonUserPrompt.Builder promptBuilder = new ComparisonUserPrompt.Builder();
        ComparisonUserPrompt prompt = promptBuilder.setOccasion(occasion).setWeather(weather).build();
        return prompt.generatePrompt();
    }

    public String sendPromptWithRetries(String extractedImagePath1, String extractedImagePath2, String userPrompt) {
        String extractedImageS3Url1 = s3Service.getFileS3Url(extractedImagePath1);
        String extractedImageS3Url2 = s3Service.getFileS3Url(extractedImagePath2);

        Supplier<String> gptResponseSupplier = () -> {
            // Attempt to send the prompt and extract the response
            String gptResponse = chatGptService.sendPromptWith2Images(extractedImageS3Url1, extractedImageS3Url2, userPrompt, SystemPrompt.COMPARISON.getPrompt());
            extractResponseForComparison(gptResponse);
            return gptResponse;
        };

        return callSupplierWithRetryService.executeWithRetries(gptResponseSupplier);
    }

    @Transactional(readOnly = true)
    public AiComparisonDetailedDto getLatestAiFeedback() {
        User user = userService.getUser();
        AiComparisonFeedback aiComparisonFeedback = aiFeedbackComparisonRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()).orElseThrow(NotFoundException::new);
        return generateComparisonAiFeedbackDto(aiComparisonFeedback);
    }

    @Transactional
    public AiComparisonDto likeStyle(LikeStyleDto likeStyleDto) {
        AiComparisonFeedback aiFeedback = aiFeedbackComparisonRepository.findById(likeStyleDto.id()).orElseThrow(NotFoundException::new);
        aiFeedback.setLikeStyle(likeStyleDto.like());
        AiComparisonFeedback savedFeedback = aiFeedbackComparisonRepository.save(aiFeedback);
        return new AiComparisonDto(savedFeedback, s3Service.getFileS3Url(savedFeedback.getImage1Path()),
                s3Service.getFileS3Url(savedFeedback.getImage2Path()), userService.getUserInfo());
    }

    @Transactional
    public void deleteAiComparisonFeedback(Long id) {
        User user = userService.getUser();
        AiComparisonFeedback aiComparisonFeedback = aiFeedbackComparisonRepository.findById(id).orElseThrow(NotFoundException::new);
        if (!Objects.equals(user.getId(), aiComparisonFeedback.getUserId())) {
            throw new BadRequestException("User does not have permission to delete this feedback");
        }
        aiFeedbackComparisonRepository.delete(aiComparisonFeedback);
    }

    @Transactional(readOnly = true)
    public Page<AiComparisonDto> listAiComparisonFeedbacks(Map<String, List<String>> tagFilters, Boolean liked, PageRequest pageRequest) {
        Sort sortBy = Sort.by(
                Sort.Order.by("created_at").with(Sort.Direction.DESC)
        );

        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), sortBy);
        UserDto userInfo = userService.getUserInfo();
        return aiFeedbackSearchService.findAiComparisonFeedbacksByTags(userInfo.id(), tagFilters, liked, pageRequestWithSort).map(comparisonFeedback ->
                new AiComparisonDto(comparisonFeedback, s3Service.getFileS3Url(comparisonFeedback.getImage1Path()),
                        s3Service.getFileS3Url(comparisonFeedback.getImage2Path()), userInfo));
    }

    @Transactional(readOnly = true)
    public AiComparisonDetailedDto getAiComparisonFeedback(Long id) {
        AiComparisonFeedback aiComparisonFeedback = aiFeedbackComparisonRepository.findById(id).orElseThrow(NotFoundException::new);
        return generateComparisonAiFeedbackDto(aiComparisonFeedback);
    }

    @Transactional
    public void reportAiComparisonFeedback(Long id, ReportAiFeedbackDto reportAiFeedbackDto) {
        AiComparisonFeedback aiComparisonFeedback = aiFeedbackComparisonRepository.findById(id).orElseThrow(NotFoundException::new);
        Long userId = userService.getUser().getId();
        ReportAiFeedback reportAiFeedback = ReportAiFeedback.fromAiComparisonFeedback(userId, aiComparisonFeedback.getId(),
                reportAiFeedbackDto.feedbackEntryId(), reportAiFeedbackDto.feedback());
        reportAiFeedbackRepository.save(reportAiFeedback);
    }

    public record AIComparisonSubmissionImage(ImageType imageType, String rawImagePath, String extractedImagePath) {}

    public record AISubmissionImagesForComparison(AIComparisonSubmissionImage image1, AIComparisonSubmissionImage image2) {}

}
