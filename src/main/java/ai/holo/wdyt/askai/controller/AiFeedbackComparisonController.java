package ai.holo.wdyt.askai.controller;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.askai.service.AiFeedbackComparisonService;
import ai.holo.wdyt.askai.service.LocationAndWeatherService;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai-comparison-feedbacks")
public class AiFeedbackComparisonController {

    private final UserService userService;
    private final LocationAndWeatherService locationAndWeatherService;
    private final AiFeedbackComparisonService aiFeedbackComparisonService;

    public AiFeedbackComparisonController(UserService userService, LocationAndWeatherService locationAndWeatherService,
                                          AiFeedbackComparisonService aiFeedbackComparisonService) {
        this.userService = userService;
        this.locationAndWeatherService = locationAndWeatherService;
        this.aiFeedbackComparisonService = aiFeedbackComparisonService;
    }

    @PostMapping(value = "/compare-two-images")
    public AiComparisonDetailedDto submitTwoImage(@RequestBody @Valid AiComparisonSubmissionDto comparisonSubmissionDto) {
        User currentUser = userService.getUser();

        AiFeedbackComparisonService.AISubmissionImagesForComparison comparisonImages = aiFeedbackComparisonService.getComparisonImages(comparisonSubmissionDto);
        LocationAndWeatherDto locationAndWeather = locationAndWeatherService.getLocationAndWeather(comparisonSubmissionDto.locationAndWeather(), comparisonSubmissionDto.clientIpAddress());

        ImageType imageType = comparisonImages.image1().imageType();
        AiSubmissionPrompt prompt = aiFeedbackComparisonService.getComparisonPrompt(comparisonSubmissionDto,
                currentUser, imageType, locationAndWeather);

        // Call ChatGPT with retries
        String gptResponse = aiFeedbackComparisonService.sendPromptWithRetries(comparisonImages.image1().extractedImagePath(), comparisonImages.image2().extractedImagePath(), prompt.promptText());

        // Save AI response
        return aiFeedbackComparisonService.saveAiCompareResponse(comparisonSubmissionDto, prompt, gptResponse, comparisonImages,  locationAndWeather);
    }

    @DeleteMapping("/{id}")
    public void deleteAiComparisonFeedback(@PathVariable("id") Long id) {
        aiFeedbackComparisonService.deleteAiComparisonFeedback(id);
    }

    @PostMapping("/like-style")
    public AiComparisonDto likeStyle(@RequestBody LikeStyleDto likeStyleDto) {
        return aiFeedbackComparisonService.likeStyle(likeStyleDto);
    }
}
