package ai.holo.wdyt.askai.controller;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.askai.service.AiFeedbackService;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/v1/ai-feedbacks")
public class AiFeedbackController {
    private final AiFeedbackService aiFeedbackService;
    private final UserService userService;

    public AiFeedbackController(AiFeedbackService aiFeedbackService, UserService userService) {
        this.aiFeedbackService = aiFeedbackService;
        this.userService = userService;
    }

    @PostMapping("/submit-image")
    public AiFeedbackDetailedDto submitImage(@RequestParam("image") MultipartFile image,
                                     @RequestParam("clientIpAddress") String clientIpAddress,
                                     @RequestParam("clientTime")ZonedDateTime clientTime) throws IOException {

        // TODO: weather and location can be received from the client
        // In that case we'll not receive ip and don't need to resolve location and weather from the ip

        UserDto userInfo = userService.getUserInfo();
        AiFeedback aiFeedback = aiFeedbackService.executeGptCall(image.getBytes(), clientIpAddress, clientTime, userInfo);
        return aiFeedbackService.saveAiResponse(aiFeedback, userInfo);
    }

    @GetMapping("/")
    public Page<AiFeedbackDto> listAiFeedbacks(@RequestParam(defaultValue = "100") Integer size,
                                               @RequestParam(defaultValue = "0") Integer page) {

        return aiFeedbackService.listAiFeedbacks(PageRequest.of(page, size));
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
}
