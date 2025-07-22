package ai.holo.wdyt.askai.controller;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.askai.service.AiFeedbackService;
import ai.holo.wdyt.askai.service.LocationAndWeatherService;
import ai.holo.wdyt.askai.service.aiprompt.SingleImageSubmissionPrompt;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai-feedbacks")
public class AiFeedbackController {
    private final AiFeedbackService aiFeedbackService;
    private final LocationAndWeatherService locationAndWeatherService;
    private final UserService userService;

    public AiFeedbackController(AiFeedbackService aiFeedbackService,
                                LocationAndWeatherService locationAndWeatherService,
                                UserService userService) {
        this.aiFeedbackService = aiFeedbackService;
        this.locationAndWeatherService = locationAndWeatherService;
        this.userService = userService;
    }

    @PostMapping(value = "/submit-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AiFeedbackDetailedDto submitImage(@RequestPart(value = "image", required = false) MultipartFile image,
                                             @RequestPart("data") String data) throws IOException {

        aiFeedbackService.checkIfUserHasEnoughCredits();
        byte[] imageBytes = image != null ? image.getBytes() : null;
        AiFeedbackSubmissionDto aiFeedbackSubmissionDto = aiFeedbackService.validateAndParseSubmissionDto(imageBytes, data);
        User currentUser = userService.getUser();

        AiFeedbackService.AISubmissionImage aiSubmissionImage = aiFeedbackService.checkImagesAndMakeNecessaryPreprocessing(imageBytes, currentUser, aiFeedbackSubmissionDto);
        LocationAndWeatherDto locationAndWeather = locationAndWeatherService.getLocationAndWeather(aiFeedbackSubmissionDto.locationAndWeather(), aiFeedbackSubmissionDto.clientIpAddress());

        String userPrompt = aiFeedbackService.preparePrompt(aiFeedbackSubmissionDto, currentUser, locationAndWeather);

        // Call ChatGPT with retries
        String gptResponse = aiFeedbackService.sendPromptWithRetries(aiSubmissionImage.extractedImagePath(), SingleImageSubmissionPrompt.generateSystemPrompt(),
                userPrompt, aiSubmissionImage.imageType());

        // Save AI response
        return aiFeedbackService.saveAiResponse(aiFeedbackSubmissionDto,gptResponse, aiSubmissionImage, locationAndWeather);
    }

    @GetMapping("/")
    public Page<AiFeedbackDto> listAiFeedbacks(@RequestParam(value = "liked", required = false) Boolean liked,
                                               @RequestParam(value = "wardrobeItemExtracted", required = false) Boolean wardrobeItemExtracted,
                                               @RequestParam(value = "color", required = false) String[] color,
                                               @RequestParam(value = "style", required = false) String[] style,
                                               @RequestParam(value = "occasion", required = false) String[] occasion,
                                               @RequestParam(value = "feedbackIdForComparison", required = false) Long feedbackIdForComparison,
                                               @RequestParam(value = "imageType", required = false) ImageType imageType,
                                               @RequestParam(value = "idsNot", required = false) List<Long> idsNot,
                                               @RequestParam(defaultValue = "100") Integer size,
                                               @RequestParam(defaultValue = "0") Integer page) {

        Map<String, List<String>> tagFilters = Map.of(
                Taggable.COLOR, color != null ? Arrays.asList(color) : List.of(),
                Taggable.STYLE, style != null ? Arrays.asList(style) : List.of(),
                Taggable.OCCASION, occasion != null ? Arrays.asList(occasion) : List.of()
        );
        return aiFeedbackService.listAiFeedbacks(tagFilters, liked, wardrobeItemExtracted, feedbackIdForComparison, idsNot, imageType, PageRequest.of(page, size));
    }

    @GetMapping("/friend/{friendId}")
    public Page<AiFeedbackDto> listFriendsAiFeedbacks(@PathVariable(value = "friendId") Long friendId,
                                                      @RequestParam(value = "liked", required = false) Boolean liked,
                                                      @RequestParam(value = "color", required = false) String[] color,
                                                      @RequestParam(value = "style", required = false) String[] style,
                                                      @RequestParam(value = "occasion", required = false) String[] occasion,
                                                      @RequestParam(value = "feedbackIdForComparison", required = false) Long feedbackIdForComparison,
                                                      @RequestParam(value = "imageType", required = false) ImageType imageType,
                                                      @RequestParam(value = "idsNot", required = false) List<Long> idsNot,
                                                      @RequestParam(defaultValue = "100") Integer size,
                                                      @RequestParam(defaultValue = "0") Integer page) {

        Map<String, List<String>> tagFilters = Map.of(
                Taggable.COLOR, color != null ? Arrays.asList(color) : List.of(),
                Taggable.STYLE, style != null ? Arrays.asList(style) : List.of(),
                Taggable.OCCASION, occasion != null ? Arrays.asList(occasion) : List.of()
        );
        return aiFeedbackService.listFriendsAiFeedbacks(friendId, tagFilters, liked, feedbackIdForComparison, idsNot, imageType, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public AiFeedbackDetailedDto getAiFeedback(@PathVariable("id") Long id) {
        return aiFeedbackService.getAiFeedback(id);
    }

    @GetMapping("/latest")
    public AiFeedbackDetailedDto getLatestAiFeedback() {
        return aiFeedbackService.getLatestAiFeedback();
    }

    @DeleteMapping("/{id}")
    public void deleteAiFeedback(@PathVariable("id") Long id) {
        aiFeedbackService.deleteAiFeedback(id);
    }

    @DeleteMapping("/{id}/feedback-entry/{feedbackEntryId}")
    public void deleteAiFeedback(@PathVariable("id") Long id, @PathVariable("feedbackEntryId") String feedbackEntryId) {
        aiFeedbackService.deleteAiFeedbackEntry(id, feedbackEntryId);
    }

    @PostMapping("/{id}/report")
    public void reportAiFeedback(@PathVariable("id") Long id,
                                 @RequestBody ReportAiFeedbackDto reportAiFeedbackDto) {
        aiFeedbackService.reportAiFeedback(id, reportAiFeedbackDto);
    }

    @PostMapping("/like-style")
    public AiFeedbackDto likeStyle(@RequestBody LikeStyleDto likeStyleDto) {
        return aiFeedbackService.likeStyle(likeStyleDto);
    }

    @GetMapping("/filters/{tag}")
    public List<String> getFilters(@PathVariable String tag) {
        return aiFeedbackService.getFilters(tag);
    }

    @GetMapping("/get-occasions")
    public List<String> getOccasions(@RequestParam(value = "filter", required = false) String filter) {
        return aiFeedbackService.getOccasions(filter);
    }
}
