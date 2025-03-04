package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.model.entity.*;
import ai.holo.wdyt.askai.model.event.AiFeedbackReceivedEvent;
import ai.holo.wdyt.askai.repository.AiFeedbackOrderRepository;
import ai.holo.wdyt.askai.repository.AiFeedbackRepository;
import ai.holo.wdyt.askai.repository.OccasionRepository;
import ai.holo.wdyt.askai.repository.ReportAiFeedbackRepository;
import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.event.service.CallSupplierWithRetryService;
import ai.holo.wdyt.common.event.service.EventPublisher;
import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.common.json.JsonUtils;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final int topListCount;
    private final PromptService promptService;
    private final AiFeedbackOrderRepository aiFeedbackOrderRepository;
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
                             @Value("${configuration.topListCount}") int topListCount,
                             PromptService promptService,
                             AiFeedbackOrderRepository aiFeedbackOrderRepository,
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
        this.topListCount = topListCount;
        this.promptService = promptService;
        this.aiFeedbackOrderRepository = aiFeedbackOrderRepository;
        this.reportAiFeedbackRepository = reportAiFeedbackRepository;
        this.aiFeedbackSearchService = aiFeedbackSearchService;
        this.occasionRepository = occasionRepository;
        this.callSupplierWithRetryService = callSupplierWithRetryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public AiSubmissionPrompt preparePrompt(AiFeedbackSubmissionDto aiFeedbackSubmissionDto, User currentUser, ImageType imageType, LocationAndWeatherDto locationAndWeather) {
        // Send prompt with image to ChatGPT
        User aiUser = aiFeedbackSubmissionDto.userId() != null ? userService.getUserById(aiFeedbackSubmissionDto.userId()) : currentUser;
        ChatGptPrompt prompt = promptService.getPrompt(imageType, SubmissionType.SINGLE);

        List<String> styles = aiFeedbackSearchService.getStyles(aiUser);
        String promptText = promptService.getPromptText(prompt, aiUser, aiFeedbackSubmissionDto.clientTime(), locationAndWeather, aiFeedbackSubmissionDto.occasions(), SubmissionType.SINGLE, styles);
        return new AiSubmissionPrompt(prompt, promptText);
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

    public String sendPromptWithRetries(String extractedImagePath, String promptText, ImageType imageType) {
        String extractedImageS3Url = s3Service.getFileS3Url(extractedImagePath);

        Supplier<String> gptResponseSupplier = () -> {
            // Attempt to send the prompt and extract the response
            String response = chatGptService.sendPromptWithImage(extractedImageS3Url, promptText);
            extractResponse(response, imageType);
            return response;
        };

        return callSupplierWithRetryService.executeWithRetries(gptResponseSupplier);
    }

    @Transactional
    public AiFeedbackDetailedDto saveAiResponse(AiFeedbackSubmissionDto aiFeedbackSubmissionDto, Long promptId,
                                                String gptResponse, AISubmissionImage aiSubmissionImage, LocationAndWeatherDto locationAndWeather, SubmissionType submissionType) {
        User currentUser = userService.getUser();
        AiFeedback feedback;
        if (aiFeedbackSubmissionDto.aiFeedbackId() != null) {
            feedback = aiFeedbackRepository.findById(aiFeedbackSubmissionDto.aiFeedbackId()).orElseThrow(NotFoundException::new);
        } else {
            AiSubmissionOrder order = getOrder(currentUser);
            feedback = new AiFeedback(currentUser.getId(), aiSubmissionImage.rawImagePath(), aiSubmissionImage.imageType(),
                    aiSubmissionImage.extractedImagePath(), order.topListOrder, order.order, submissionType);

        }
        User aiUser = aiFeedbackSubmissionDto.userId() != null ? userService.getUserById(aiFeedbackSubmissionDto.userId()) : currentUser;
        feedback.addFeedbackEntry(new FeedbackEntry(UUID.randomUUID().toString(), aiUser.getId(), promptId, gptResponse, null, locationAndWeather, LocalDateTime.now()));

        Pair<OutfitAnalysis, HeadStyleAnalysis> analysis = extractResponse(gptResponse, aiSubmissionImage.imageType());
        Map<String, List<String>> tags = getTags(analysis);

        feedback.updateTags(tags);
        AiFeedback savedAiFeedback = aiFeedbackRepository.save(feedback);

        eventPublisher.publishEvent(new AiFeedbackReceivedEvent(savedAiFeedback.getId(), currentUser.getId()));

        return generateAiFeedbackDto(savedAiFeedback);
    }


    private Map<String, List<String>> getTags(Pair<OutfitAnalysis, HeadStyleAnalysis> analysis) {
        Taggable taggable = analysis.getLeft() != null ? analysis.getLeft() : analysis.getRight();
        return taggable.getTags();
    }

    private AiSubmissionOrder getOrder(User user) {
        Integer topListOrder = null;
        Integer order = null;
        int itemsInTopList = aiFeedbackRepository.countByUserIdAndTopListOrderIsNotNull(user.getId());
        if (itemsInTopList < topListCount) {
            topListOrder = itemsInTopList + 1;
        }
        // if item is not in top list, increment order
        if (topListOrder == null) {
            AiFeedbackOrder aiFeedbackOrder = aiFeedbackOrderRepository.findByUserId(user.getId()).orElse(new AiFeedbackOrder(user.getId()));
            order = aiFeedbackOrder.incrementOrder();
            aiFeedbackOrderRepository.save(aiFeedbackOrder);
        }
        return new AiSubmissionOrder(topListOrder, order);
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
        s3Service.saveImage(extractedImage, path);
        return path;
    }

    private String saveRawImageOnS3(InputStream image, User user, long currentTimeMillis) {
        String path = String.format("%d/%d/raw_%d.png", user.getId(), currentTimeMillis, currentTimeMillis);
        s3Service.saveImage(image, path);
        return path;
    }

    @Transactional(readOnly = true)
    public Page<AiFeedbackDto> listAiFeedbacks(Map<String, List<String>> tagFilters, Boolean liked, Long excludeUserId, PageRequest pageRequest) {
        Sort sortBy = Sort.by(
                Sort.Order.by("top_list_order").with(Sort.Direction.DESC), // `topListOrder` prioritized
                Sort.Order.by("standard_order").with(Sort.Direction.DESC)         // Then by `order`
        );

        PageRequest pageRequestWithSort = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), sortBy);
        UserDto userInfo = userService.getUserInfo();
        return aiFeedbackSearchService.findAiFeedbacksByTags(userInfo.id(), tagFilters, liked, excludeUserId, pageRequestWithSort).map(aiFeedback ->
                new AiFeedbackDto(aiFeedback, s3Service.getFileS3Url(aiFeedback.getExtractedImagePath()), userInfo));
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
        return new AiFeedbackDto(savedFeedback, s3Service.getFileS3Url(savedFeedback.getExtractedImagePath()), userService.getUserInfo());
    }

    @Transactional
    public AiFeedbackDto likeAiResponse(LikeAiResponseDto likeAiResponseDto) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(likeAiResponseDto.id()).orElseThrow(NotFoundException::new);
        checkFeedbackIsExistingAndNotLikedAlready(likeAiResponseDto, aiFeedback);
        // Update feedback entry
        List<FeedbackEntry> updatedFeedbackEntries = aiFeedback.getFeedbackEntries().stream().map(feedback -> {
            if (feedback.id().equals(likeAiResponseDto.feedbackId())) {
                return new FeedbackEntry(feedback.id(), feedback.userId(), feedback.promptId(), feedback.response(), true, feedback.locationAndWeather(), feedback.createdAt());
            }
            return feedback;
        }).toList();
        aiFeedback.setFeedbackEntries(updatedFeedbackEntries);
        AiFeedback savedFeedback = aiFeedbackRepository.save(aiFeedback);
        return new AiFeedbackDto(savedFeedback, s3Service.getFileS3Url(savedFeedback.getExtractedImagePath()), userService.getUserInfo());
    }

    private void checkFeedbackIsExistingAndNotLikedAlready(LikeAiResponseDto likeAiResponseDto, AiFeedback aiFeedback) {
        FeedbackEntry feedbackEntry = aiFeedback.getFeedbackEntries().stream()
                .filter(feedback -> feedback.id().equals(likeAiResponseDto.feedbackId()))
                .findFirst()
                .orElseThrow(NotFoundException::new);
        if (feedbackEntry.likeAiResponse() != null) {
            throw new BadRequestException("Feedback already liked");
        }
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
    public void pinOnTheTopList(PinAiFeedbackDto pinAiFeedbackDto) {
        User user = userService.getUser();
        AiFeedback aiFeedback = aiFeedbackRepository.findByIdAndUserId(pinAiFeedbackDto.aiFeedbackId(), user.getId())
                .orElseThrow(NotFoundException::new);

        if (pinAiFeedbackDto.pin()) {
            pin(aiFeedback, user);
        } else {
            unpin(aiFeedback, user);
        }
    }

    private void pin(AiFeedback aiFeedback, User user) {
        List<AiFeedback> itemsInTopList = aiFeedbackRepository.findByUserIdAndTopListOrderIsNotNullOrderByTopListOrderAsc(user.getId());
        // Remove items from top list if it exceeds the limit
        while (itemsInTopList.size() > topListCount - 1) {
            AiFeedback lastItem = itemsInTopList.get(itemsInTopList.size() - 1);
            unpin(lastItem, user);
            itemsInTopList.remove(lastItem);
        }
        AtomicInteger topListIndex = new AtomicInteger(2);
        itemsInTopList.forEach(item -> item.setTopListOrder(topListIndex.getAndIncrement()));
        aiFeedback.setTopListOrder(1);
        aiFeedback.setOrder(null);
        itemsInTopList.add(aiFeedback);
        aiFeedbackRepository.saveAll(itemsInTopList);
    }

    private void unpin(AiFeedback aiFeedback, User user) {
        AiSubmissionOrder order = getOrder(user);

        aiFeedback.setTopListOrder(null);
        aiFeedback.setOrder(order.order());
        aiFeedbackRepository.save(aiFeedback);
    }

    @Transactional
    public void deleteAiFeedback(Long id) {
        aiFeedbackRepository.deleteById(id);
    }

    @Transactional
    public void reportAiFeedback(Long id, ReportAiFeedbackDto reportAiFeedbackDto) {
        AiFeedback aiFeedback = aiFeedbackRepository.findById(id).orElseThrow(NotFoundException::new);
        Long userId = userService.getUser().getId();
        ReportAiFeedback reportAiFeedback = new ReportAiFeedback(userId, aiFeedback.getId(), reportAiFeedbackDto.feedback());
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
    public record AISubmissionImage(ImageType imageType, String rawImagePath, String extractedImagePath) {}
}
