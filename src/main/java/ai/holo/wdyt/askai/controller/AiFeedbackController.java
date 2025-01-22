package ai.holo.wdyt.askai.controller;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.askai.service.AiFeedbackService;
import ai.holo.wdyt.user.model.dto.UserDto;
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
    private final UserService userService;

    public AiFeedbackController(AiFeedbackService aiFeedbackService, UserService userService) {
        this.aiFeedbackService = aiFeedbackService;
        this.userService = userService;
    }

    @PostMapping(value = "/submit-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AiFeedbackDetailedDto submitImage(@RequestPart("image") MultipartFile image,
                                             @RequestPart("data") String data) throws IOException {

        UserDto userInfo = userService.getUserInfo();
        AiFeedback aiFeedback = aiFeedbackService.executeGptCall(image.getBytes(), userInfo, data);
        return aiFeedbackService.saveAiResponse(aiFeedback, userInfo);
    }

    @GetMapping("/")
    public Page<AiFeedbackDto> listAiFeedbacks(@RequestParam(value = "liked", required = false) Boolean liked,
                                               @RequestParam(value = "color", required = false) String[] color,
                                               @RequestParam(value = "style", required = false) String[] style,
                                               @RequestParam(value = "occasion", required = false) String[] occasion,
                                               @RequestParam(defaultValue = "100") Integer size,
                                               @RequestParam(defaultValue = "0") Integer page) {

        Map<String, List<String>> tagFilters = Map.of(
                "color", color != null ? Arrays.asList(color) : List.of(),
                "style", style != null ? Arrays.asList(style) : List.of(),
                "occasion", occasion != null ? Arrays.asList(occasion) : List.of()
        );
        return aiFeedbackService.listAiFeedbacks(tagFilters, liked, PageRequest.of(page, size));
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
