package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.model.entity.*;
import ai.holo.wdyt.askai.model.event.AiFeedbackReceivedEvent;
import ai.holo.wdyt.askai.repository.AiFeedbackRepository;
import ai.holo.wdyt.askai.repository.OccasionRepository;
import ai.holo.wdyt.askai.repository.ReportAiFeedbackRepository;
import ai.holo.wdyt.askai.service.aiprompt.SingleImageSubmissionPrompt;
import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.event.service.CallSupplierWithRetryService;
import ai.holo.wdyt.common.event.service.EventPublisher;
import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.common.exception.InsufficientCreditException;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.common.exception.PrivateAccountException;
import ai.holo.wdyt.common.json.JsonUtils;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.subscription.service.UserCreditService;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

@Service
@Slf4j
public class AiFeedbackService {
    private final ChatGptService chatGptService;
    private final S3Service s3Service;
    private final PhotoroomBgExtractionService photoroomBgExtractionService;
    private final ImageClassificationService imageClassificationService;
    private final UserService userService;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final ReportAiFeedbackRepository reportAiFeedbackRepository;
    private final AiFeedbackSearchService aiFeedbackSearchService;
    private final OccasionRepository occasionRepository;
    private final CallSupplierWithRetryService callSupplierWithRetryService;
    private final EventPublisher eventPublisher;

    public AiFeedbackService(ChatGptService chatGptService, S3Service s3Service,
                             PhotoroomBgExtractionService photoroomBgExtractionService,
                             ImageClassificationService imageClassificationService,
                             UserService userService,
                             AiFeedbackRepository aiFeedbackRepository,
                             ReportAiFeedbackRepository reportAiFeedbackRepository,
                             AiFeedbackSearchService aiFeedbackSearchService,
                             OccasionRepository occasionRepository,
                             CallSupplierWithRetryService callSupplierWithRetryService,
                             EventPublisher eventPublisher) {
        this.chatGptService = chatGptService;
        this.s3Service = s3Service;
        this.photoroomBgExtractionService = photoroomBgExtractionService;
        this.imageClassificationService = imageClassificationService;
        this.userService = userService;
        this.aiFeedbackRepository = aiFeedbackRepository;
        this.reportAiFeedbackRepository = reportAiFeedbackRepository;
        this.aiFeedbackSearchService = aiFeedbackSearchService;
        this.occasionRepository = occasionRepository;
        this.callSupplierWithRetryService = callSupplierWithRetryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public String preparePrompt(AiFeedbackSubmissionDto aiFeedbackSubmissionDto, User currentUser, LocationAndWeatherDto locationAndWeather) {
        // Send prompt with image to ChatGPT
        User aiUser = aiFeedbackSubmissionDto.userId() != null ? userService.getUserById(aiFeedbackSubmissionDto.userId()) : currentUser;

        List<String> styles = aiFeedbackSearchService.getStylesBasedOnUserStyleAdaptedPreference(aiUser);
        List<String> colors = aiFeedbackSearchService.findDistinctTagsFromAiFeedbackAndComparisonByUserIdAndTag(currentUser.getId(), "colorCode");

        String location = locationAndWeather.location().getLocation();

        List<String> occasions = aiFeedbackSubmissionDto.occasions() == null || aiFeedbackSubmissionDto.occasions().isEmpty()
                ? aiFeedbackSearchService.findDistinctTagsFromAiFeedbackAndComparisonByUserIdAndTag(currentUser.getId(), "occasion")
                : aiFeedbackSubmissionDto.occasions();

        SingleImageSubmissionPrompt.Builder builder = new SingleImageSubmissionPrompt.Builder();
        SingleImageSubmissionPrompt prompt = builder.useStyles(styles).useColors(colors).useCurrentDate().useOccasion(occasions.get(0)).useLocation(location).build();

        return prompt.generatePrompt();
    }

    public AiFeedbackSubmissionDto validateAndParseSubmissionDto(byte[] image, String data) throws JsonProcessingException {
        AiFeedbackSubmissionDto aiFeedbackSubmissionDto = parseJson(data);
        if (aiFeedbackSubmissionDto.clientTime() == null) {
            throw new BadRequestException("clientTime is required");
        }
        checkImageOrPreviousSubmissionIdIsProvided(image, aiFeedbackSubmissionDto);
        checkTheProvidedUserIsFriendWithTheCurrentUser(aiFeedbackSubmissionDto);
        checkFeedbackNotExistingForTheUser(aiFeedbackSubmissionDto);
        return aiFeedbackSubmissionDto;
    }

    private void checkFeedbackNotExistingForTheUser(AiFeedbackSubmissionDto aiFeedbackSubmissionDto) {
        if (aiFeedbackSubmissionDto.aiFeedbackId() != null) {
            Long aiUserId = aiFeedbackSubmissionDto.userId() != null ? aiFeedbackSubmissionDto.userId() : userService.getUser().getId();
            AiFeedback aiFeedback = aiFeedbackRepository.findById(aiFeedbackSubmissionDto.aiFeedbackId()).orElseThrow(NotFoundException::new);
            boolean userFeedbackExists = aiFeedback.getFeedbackEntries().stream().anyMatch(feedback -> feedback.userId().equals(aiUserId));
            if (userFeedbackExists) {
                throw new BadRequestException("Feedback already exists for the user");
            }
        }
    }

    private void checkTheProvidedUserIsFriendWithTheCurrentUser(AiFeedbackSubmissionDto aiFeedbackSubmissionDto) {
        if (aiFeedbackSubmissionDto.userId() != null) {
            boolean isFriend = userService.isCurrentUserFriendWith(aiFeedbackSubmissionDto.userId());
            if (!isFriend) {
                throw new BadRequestException("User is not a friend");
            }
        }
    }

    public AISubmissionImage checkImagesAndMakeNecessaryPreprocessing(byte[] image, User currentUser, AiFeedbackSubmissionDto aiFeedbackSubmissionDto) {
        if (aiFeedbackSubmissionDto.aiFeedbackId() != null) {
            AiFeedback aiFeedback = aiFeedbackRepository.findById(aiFeedbackSubmissionDto.aiFeedbackId()).orElseThrow(NotFoundException::new);
            return new AISubmissionImage(aiFeedback.getImageType(), aiFeedback.getRawImagePath(), aiFeedback.getExtractedImagePath());
        }

        long currentTimeMillis = System.currentTimeMillis();
        // Save Raw image
        String rawImagePath = saveRawImageOnS3(new ByteArrayInputStream(image), currentUser, currentTimeMillis);

        // Classify image
        ImageType imageType = imageClassificationService.classifyImage(image);
        if (imageType == ImageType.OTHER) {
            throw new BadRequestException("Provided image is not appropriate for AI processing.");
        }

        String extractedImagePath = rawImagePath;
        if (!aiFeedbackSubmissionDto.bgExtracted()) {
            // Extract background and save extracted image
            InputStream extractedImage = photoroomBgExtractionService.extractBackground(image, rawImagePath);
            extractedImagePath = saveExtractedImageOnS3(currentUser, currentTimeMillis, extractedImage);
        }
        return new AISubmissionImage(imageType, rawImagePath, extractedImagePath);
    }

    private void checkImageOrPreviousSubmissionIdIsProvided(byte[] image, AiFeedbackSubmissionDto aiFeedbackSubmissionDto) {
        if (image == null && aiFeedbackSubmissionDto.aiFeedbackId() == null) {
            throw new BadRequestException("Either image or previous submission id is required");
        }
    }

    private AiFeedbackSubmissionDto parseJson(String data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(data, AiFeedbackSubmissionDto.class);
    }

    public String sendPromptWithRetries(String extractedImagePath, String systemPrompt, String userPrompt, ImageType imageType) {
        String extractedImageS3Url = s3Service.getFileS3Url(extractedImagePath);

        Supplier<String> gptResponseSupplier = () -> {
            // Attempt to send the prompt and extract the response
            String response = chatGptService.sendPromptWithImage(extractedImageS3Url, systemPrompt, userPrompt);
            extractResponse(response, imageType);
            return response;
        };

        return callSupplierWithRetryService.executeWithRetries(gptResponseSupplier);
    }

    @Transactional
    public AiFeedbackDetailedDto saveAiResponse(AiFeedbackSubmissionDto aiFeedbackSubmissionDto,
                                                String gptResponse, AISubmissionImage aiSubmissionImage,
                                                LocationAndWeatherDto locationAndWeather) {
        User currentUser = userService.getUser();
        AiFeedback feedback;
        if (aiFeedbackSubmissionDto.aiFeedbackId() != null) {
            feedback = aiFeedbackRepository.findById(aiFeedbackSubmissionDto.aiFeedbackId()).orElseThrow(NotFoundException::new);
        } else {
            feedback = new AiFeedback(currentUser.getId(), aiSubmissionImage.rawImagePath(), aiSubmissionImage.imageType(),
                    aiSubmissionImage.extractedImagePath());

        }
        User aiUser = aiFeedbackSubmissionDto.userId() != null ? userService.getUserById(aiFeedbackSubmissionDto.userId()) : currentUser;
        feedback.addFeedbackEntry(new FeedbackEntry(UUID.randomUUID().toString(), aiUser.getId(), gptResponse, locationAndWeather, LocalDateTime.now()));
        Pair<OutfitAnalysis, HeadStyleAnalysis> analysis = extractResponse(gptResponse, aiSubmissionImage.imageType());
        Map<String, List<String>> tags = getTags(analysis);

        feedback.updateTags(tags);
        updateLastSubmissionDate(feedback);
        AiFeedback savedAiFeedback = aiFeedbackRepository.save(feedback);

        eventPublisher.publishEvent(new AiFeedbackReceivedEvent(savedAiFeedback.getId(), currentUser.getId(), aiUser.getId()));

        return generateAiFeedbackDto(savedAiFeedback);
    }

    private Map<String, List<String>> getTags(Pair<OutfitAnalysis, HeadStyleAnalysis> analysis) {
        Taggable taggable = analysis.getLeft() != null ? analysis.getLeft() : analysis.getRight();
        return taggable.getTags();
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

    private String saveExtractedImageOnS3(User user, long currentTimeMillis, InputStream extractedImage) {
        String path = String.format("%d/%d/extracted_%d.png", user.getId(), currentTimeMillis, currentTimeMillis);
        return s3Service.saveImage(extractedImage, path);
    }

    private String saveRawImageOnS3(InputStream image, User user, long currentTimeMillis) {
        String path = String.format("%d/%d/raw_%d.png", user.getId(), currentTimeMillis, currentTimeMillis);
        return s3Service.saveImage(image, path);
    }

    @Transactional(readOnly = true)
    public Page<AiFeedbackDto> listAiFeedbacks(Map<String, List<String>> tagFilters, Boolean liked,
                                               Long feedbackIdForComparison, List<Long> idsNot, ImageType imageType,
                                               PageRequest pageRequest) {
        Sort sortBy = Sort.by(
                Sort.Order.by("created_at").with(Sort.Direction.DESC)
        );

        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), sortBy);
        UserDto userInfo = userService.getUserInfo();
        return aiFeedbackSearchService.findAiFeedbacksByTags(userInfo.id(), tagFilters, liked,
                feedbackIdForComparison, idsNot, imageType, pageRequestWithSort).map(aiFeedback ->
                new AiFeedbackDto(aiFeedback, s3Service.getFileS3Url(aiFeedback.getExtractedImagePath()), userInfo));
    }

    @Transactional(readOnly = true)
    public Page<AiFeedbackDto> listFriendsAiFeedbacks(Long friendId, Map<String, List<String>> tagFilters, Boolean liked,
                                               Long feedbackIdForComparison, List<Long> idsNot, ImageType imageType,
                                               PageRequest pageRequest) {
        User friend = userService.getUserById(friendId);
        UserDto friendInfo = new UserDto(friend);

        if (!userService.isCurrentUserFriendWith(friendId)) {
            throw new BadRequestException("User is not a friend");
        }

        if (!friend.isPublicProfile()){
            throw new PrivateAccountException("This profile is private !");
        }

        Sort sortBy = Sort.by(
                Sort.Order.by("created_at").with(Sort.Direction.DESC)
        );
        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), sortBy);
        return aiFeedbackSearchService.findAiFeedbacksByTags(friendInfo.id(), tagFilters, liked,
                feedbackIdForComparison, idsNot, imageType, pageRequestWithSort).map(aiFeedback ->
                new AiFeedbackDto(aiFeedback, s3Service.getFileS3Url(aiFeedback.getExtractedImagePath()), friendInfo));
    }

    @Transactional
    public AiFeedbackDto likeStyle(LikeStyleDto likeStyleDto) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(likeStyleDto.id()).orElseThrow(NotFoundException::new);
        aiFeedback.setLikeStyle(likeStyleDto.like());
        AiFeedback savedFeedback = aiFeedbackRepository.save(aiFeedback);
        return new AiFeedbackDto(savedFeedback, s3Service.getFileS3Url(savedFeedback.getExtractedImagePath()), userService.getUserInfo());
    }

    @Transactional(readOnly = true)
    public AiFeedbackDetailedDto getAiFeedback(Long id) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(id).orElseThrow(NotFoundException::new);
        User user = userService.getUser();
        if (!aiFeedback.getUserId().equals(user.getId())) {
            throw new NotFoundException();
        }
        return generateAiFeedbackDto(aiFeedback);
    }

    @Transactional(readOnly = true)
    public AiFeedbackDetailedDto getLatestAiFeedback() {
        User user = userService.getUser();
        AiFeedback aiFeedback = aiFeedbackRepository.findFirstByUserIdOrderByLastFeedbackReceivedAtDesc(user.getId()).orElseThrow(NotFoundException::new);
        return generateAiFeedbackDto(aiFeedback);
    }

    private AiFeedbackDetailedDto generateAiFeedbackDto(AiFeedback aiFeedback) {
        List<FeedbackEntryDto> feedbackEntryDtos = aiFeedback.getFeedbackEntries().stream().map(feedback -> {
            Pair<OutfitAnalysis, HeadStyleAnalysis> analysis = extractResponse(feedback.response(), aiFeedback.getImageType());
            User aiUser = userService.getUserById(feedback.userId());
            return new FeedbackEntryDto(feedback, analysis.getLeft(), analysis.getRight(), null, new UserDto(aiUser));
        }).toList();
        return new AiFeedbackDetailedDto(aiFeedback, s3Service.getFileS3Url(aiFeedback.getExtractedImagePath()),
                userService.getUserInfo(), feedbackEntryDtos);
    }

    @Transactional
    public void deleteAiFeedback(Long id) {
        AiFeedback aiFeedback = checkFeedbackBelongsToLoggedInUserAndReturn(id);
        aiFeedbackRepository.delete(aiFeedback);
    }

    private AiFeedback checkFeedbackBelongsToLoggedInUserAndReturn(Long id) {
        User user = userService.getUser();
        AiFeedback aiFeedback = aiFeedbackRepository.findById(id).orElseThrow(NotFoundException::new);
        if (!Objects.equals(user.getId(), aiFeedback.getUserId())) {
            throw new BadRequestException("User does not have permission to delete this feedback");
        }
        return aiFeedback;
    }

    @Transactional
    public void reportAiFeedback(Long id, ReportAiFeedbackDto reportAiFeedbackDto) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(id).orElseThrow(NotFoundException::new);
        Long userId = userService.getUser().getId();
        ReportAiFeedback reportAiFeedback = ReportAiFeedback.fromAiFeedback(userId, aiFeedback.getId(),
                reportAiFeedbackDto.feedbackEntryId(), reportAiFeedbackDto.feedback());
        reportAiFeedbackRepository.save(reportAiFeedback);
    }

    @Transactional(readOnly = true)
    public List<String> getFilters(String tag) {
        User user = userService.getUser();
        return aiFeedbackSearchService.findDistinctTagsFromAiFeedbackByUserIdAndTag(user.getId(), tag);
    }

    public List<String> getOccasions(String filter) {
        return occasionRepository.searchByFreeText(filter).stream().map(Occasion::getName).toList();
    }

    @Transactional
    public void deleteAiFeedbackEntry(Long id, String feedbackEntryId) {
        AiFeedback aiFeedback = checkFeedbackBelongsToLoggedInUserAndReturn(id);
        FeedbackEntry feedbackEntry = aiFeedback.getFeedbackEntries().stream()
                .filter(entry -> entry.id().equals(feedbackEntryId))
                .findFirst()
                .orElseThrow(NotFoundException::new);
        aiFeedback.removeFeedbackEntry(feedbackEntry);
        updateLastSubmissionDate(aiFeedback);
        saveOrDeleteAiFeedbackBasedOnEntries(aiFeedback);
    }

    private void saveOrDeleteAiFeedbackBasedOnEntries(AiFeedback aiFeedback) {
        if (aiFeedback.getFeedbackEntries().isEmpty()) {
            aiFeedbackRepository.delete(aiFeedback);
        } else {
            aiFeedbackRepository.save(aiFeedback);
        }
    }

    public void checkIfUserHasEnoughCredits() {
        boolean userHasEnoughCredits = userService.getUser().getCreditBalance() >= UserCreditService.AI_FEEDBACK_COST;
        if (!userHasEnoughCredits) {
            throw new InsufficientCreditException(userService.getUser().getId());
        }
    }

    private void updateLastSubmissionDate(AiFeedback aiFeedback) {
        aiFeedback.getFeedbackEntries().stream()
                .max(Comparator.comparing(FeedbackEntry::createdAt))
                .ifPresentOrElse(feedbackEntry -> aiFeedback.setLastFeedbackReceivedAt(feedbackEntry.createdAt()),
                        () -> aiFeedback.setLastFeedbackReceivedAt(null)
                );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AIResponsePayload(String id, List<AIResponseAssistantMessage> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AIResponseAssistantMessage(AIMessage message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AIMessage(String content) {
    }

    public record AISubmissionImage(ImageType imageType, String rawImagePath, String extractedImagePath) {
    }
}
