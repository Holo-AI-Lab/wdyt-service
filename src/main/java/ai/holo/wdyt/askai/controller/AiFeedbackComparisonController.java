package ai.holo.wdyt.askai.controller;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.service.AiFeedbackComparisonService;
import ai.holo.wdyt.askai.service.LocationAndWeatherService;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai-comparison-feedbacks")
public class AiFeedbackComparisonController {

    private final LocationAndWeatherService locationAndWeatherService;
    private final AiFeedbackComparisonService aiFeedbackComparisonService;

    public AiFeedbackComparisonController(LocationAndWeatherService locationAndWeatherService,
                                          AiFeedbackComparisonService aiFeedbackComparisonService) {
        this.locationAndWeatherService = locationAndWeatherService;
        this.aiFeedbackComparisonService = aiFeedbackComparisonService;
    }

    @PostMapping(value = "/compare-two-outfits")
    public AiComparisonDetailedDto compareTwoOutfits(@RequestBody @Valid AiComparisonSubmissionDto comparisonSubmissionDto) {
        aiFeedbackComparisonService.checkIfUserHasEnoughCredits();

        AiFeedbackComparisonService.AISubmissionImagesForComparison comparisonImages = aiFeedbackComparisonService.getComparisonImages(comparisonSubmissionDto);
        LocationAndWeatherDto locationAndWeather = locationAndWeatherService.getLocationAndWeather(comparisonSubmissionDto.locationAndWeather(), comparisonSubmissionDto.clientIpAddress());

        String prompt = aiFeedbackComparisonService.getComparisonPrompt(comparisonSubmissionDto, locationAndWeather);

        // Call ChatGPT with retries
        String gptResponse = aiFeedbackComparisonService.sendPromptWithRetries(comparisonImages.image1().extractedImagePath(), comparisonImages.image2().extractedImagePath(), prompt);

        // Save AI response
        return aiFeedbackComparisonService.saveAiCompareResponse(comparisonSubmissionDto, gptResponse, comparisonImages,  locationAndWeather);
    }

    @GetMapping("/")
    public Page<AiComparisonDto> listAiFeedbacks(@RequestParam(value = "liked", required = false) Boolean liked,
                                               @RequestParam(value = "color", required = false) String[] color,
                                               @RequestParam(value = "style", required = false) String[] style,
                                               @RequestParam(value = "occasion", required = false) String[] occasion,
                                               @RequestParam(defaultValue = "100") Integer size,
                                               @RequestParam(defaultValue = "0") Integer page) {

        Map<String, List<String>> tagFilters = Map.of(
                Taggable.COLOR, color != null ? Arrays.asList(color) : List.of(),
                Taggable.STYLE, style != null ? Arrays.asList(style) : List.of(),
                Taggable.OCCASION, occasion != null ? Arrays.asList(occasion) : List.of()
        );
        return aiFeedbackComparisonService.listAiComparisonFeedbacks(tagFilters, liked, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public AiComparisonDetailedDto getAiComparisonFeedback(@PathVariable("id") Long id) {
        return aiFeedbackComparisonService.getAiComparisonFeedback(id);
    }

    @DeleteMapping("/{id}")
    public void deleteAiComparisonFeedback(@PathVariable("id") Long id) {
        aiFeedbackComparisonService.deleteAiComparisonFeedback(id);
    }

    @PostMapping("/like-style")
    public AiComparisonDto likeStyle(@RequestBody LikeStyleDto likeStyleDto) {
        return aiFeedbackComparisonService.likeStyle(likeStyleDto);
    }

    @PostMapping("/{id}/report")
    public void reportAiComparisonFeedback(@PathVariable("id") Long id,
                                 @RequestBody ReportAiFeedbackDto reportAiFeedbackDto) {
        aiFeedbackComparisonService.reportAiComparisonFeedback(id, reportAiFeedbackDto);
    }

    @GetMapping("/latest")
    public AiComparisonDetailedDto getLatestAiFeedback() {
        return aiFeedbackComparisonService.getLatestAiFeedback();
    }
}
