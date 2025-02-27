package ai.holo.wdyt.askai.controller;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.model.entity.SubmissionType;
import ai.holo.wdyt.askai.service.AiFeedbackService;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import org.apache.coyote.BadRequestException;
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
    private final UserService userService;

    public AiFeedbackController(AiFeedbackService aiFeedbackService, UserService userService) {
        this.aiFeedbackService = aiFeedbackService;
        this.userService = userService;
    }

    @PostMapping(value = "/submit-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AiFeedbackDetailedDto submitImage(@RequestPart(value = "image", required = false) MultipartFile image,
                                             @RequestPart("data") String data) throws IOException {

        byte[] imageBytes = image != null ? image.getBytes() : null;
        AiFeedbackSubmissionDto aiFeedbackSubmissionDto = aiFeedbackService.validateAndParseSubmissionDto(imageBytes, data);
        User currentUser = userService.getUser();

        AiFeedbackService.AISubmissionImage aiSubmissionImage = aiFeedbackService.checkImagesAndMakeNecessaryPreprocessing(imageBytes, currentUser, aiFeedbackSubmissionDto);
        LocationAndWeatherDto locationAndWeather = aiFeedbackService.getLocationAndWeather(aiFeedbackSubmissionDto);

        AiFeedbackService.AiSubmissionPrompt prompt = aiFeedbackService.preparePrompt(aiFeedbackSubmissionDto, currentUser, aiSubmissionImage, locationAndWeather, SubmissionType.SINGLE);

        // Call ChatGPT with retries
        String gptResponse = aiFeedbackService.sendPromptWithRetries(aiSubmissionImage.extractedImagePath(), prompt.promptText(), aiSubmissionImage.imageType());

        // Save AI response
        return aiFeedbackService.saveAiResponse(aiFeedbackSubmissionDto, prompt.prompt().getId(), gptResponse, aiSubmissionImage, locationAndWeather, SubmissionType.SINGLE);
    }

    @PostMapping(value = "/submit-two-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AiFeedbackDetailedDto submitTwoImage(@RequestPart(value = "image1", required = false) MultipartFile image1,
                                                @RequestPart(value = "image2", required = false) MultipartFile image2,
                                                @RequestPart("data") String data) throws IOException {

        byte[] imageBytes1 = image1 != null ? image1.getBytes() : null;
        byte[] imageBytes2 = image2 != null ? image2.getBytes() : null;

        AiFeedbackSubmissionDto aiFeedbackSubmissionDto = aiFeedbackService.validateAndParseSubmissionDto(imageBytes1, data);

        User currentUser = userService.getUser();

        AiFeedbackService.AISubmissionImage aiSubmissionImage1 = aiFeedbackService.checkImagesAndMakeNecessaryPreprocessing(imageBytes1, currentUser, aiFeedbackSubmissionDto);
        AiFeedbackService.AISubmissionImage aiSubmissionImage2 = aiFeedbackService.checkImagesAndMakeNecessaryPreprocessing(imageBytes2, currentUser, aiFeedbackSubmissionDto);
        if (aiSubmissionImage1.imageType() != aiSubmissionImage2.imageType()){
            throw new BadRequestException("Provided images are not same image type.");
        }
        LocationAndWeatherDto locationAndWeather = aiFeedbackService.getLocationAndWeather(aiFeedbackSubmissionDto);

        AiFeedbackService.AiSubmissionPrompt prompt = aiFeedbackService.preparePrompt(aiFeedbackSubmissionDto,
                currentUser, aiSubmissionImage1, locationAndWeather, SubmissionType.COMPARE);

        // Call ChatGPT with retries
        String gptResponse = aiFeedbackService.sendPromptWithRetries(aiSubmissionImage1.extractedImagePath(), aiSubmissionImage2.extractedImagePath(), prompt.promptText(), aiSubmissionImage1.imageType());

        // Save AI response
        return aiFeedbackService.saveAiCompareResponse(aiFeedbackSubmissionDto, prompt.prompt().getId(), gptResponse, aiSubmissionImage1, aiSubmissionImage2,  locationAndWeather, SubmissionType.COMPARE);
    }

    @GetMapping("/")
    public Page<AiFeedbackDto> listAiFeedbacks(@RequestParam(value = "liked", required = false) Boolean liked,
                                               @RequestParam(value = "color", required = false) String[] color,
                                               @RequestParam(value = "style", required = false) String[] style,
                                               @RequestParam(value = "excludeFeedbacksWithUserId", required = false) Long excludeUserId,
                                               @RequestParam(value = "occasion", required = false) String[] occasion,
                                               @RequestParam(defaultValue = "100") Integer size,
                                               @RequestParam(defaultValue = "0") Integer page) {

        Map<String, List<String>> tagFilters = Map.of(
                "color", color != null ? Arrays.asList(color) : List.of(),
                "style", style != null ? Arrays.asList(style) : List.of(),
                "occasion", occasion != null ? Arrays.asList(occasion) : List.of()
        );
        return aiFeedbackService.listAiFeedbacks(tagFilters, liked, excludeUserId, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public AiFeedbackDetailedDto getAiFeedback(@PathVariable("id") Long id) {
        return aiFeedbackService.getAiFeedback(id);
    }

    @DeleteMapping("/{id}")
    public void deleteAiFeedback(@PathVariable("id") Long id) {
        aiFeedbackService.deleteAiFeedback(id);
    }

    @PostMapping("/{id}/report")
    public void reportAiFeedback(@PathVariable("id") Long id,
                                 @RequestBody ReportAiFeedbackDto reportAiFeedbackDto) {
        aiFeedbackService.reportAiFeedback(id, reportAiFeedbackDto);
    }

    @PostMapping("/swap")
    public void swapAiFeedbacks(@RequestBody SwapAiFeedbackDto swapAiFeedbackDto) {
        aiFeedbackService.swapFeedbackOrders(swapAiFeedbackDto);
    }

    @PostMapping("/pin")
    public void pinFromTopList(@RequestBody PinAiFeedbackDto pinAiFeedbackDto) {
        aiFeedbackService.pinOnTheTopList(pinAiFeedbackDto);
    }

    @PostMapping("/like-style")
    public AiFeedbackDto likeStyle(@RequestBody LikeStyleDto likeStyleDto) {
        return aiFeedbackService.likeStyle(likeStyleDto);
    }

    @PostMapping("/like-ai-response")
    public AiFeedbackDto likeAiResponse(@RequestBody LikeAiResponseDto likeAiResponseDto) {
        return aiFeedbackService.likeAiResponse(likeAiResponseDto);
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
