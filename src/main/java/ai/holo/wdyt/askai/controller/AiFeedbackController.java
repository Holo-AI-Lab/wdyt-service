package ai.holo.wdyt.askai.controller;

import ai.holo.wdyt.askai.model.dto.*;
import ai.holo.wdyt.askai.service.AiFeedbackService;
import jakarta.websocket.server.PathParam;
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

    public AiFeedbackController(AiFeedbackService aiFeedbackService) {
        this.aiFeedbackService = aiFeedbackService;
    }

    @PostMapping("/submit-image")
    public AiFeedbackDetailedDto submitImage(@RequestParam("image") MultipartFile image,
                                     @RequestParam("clientIpAddress") String clientIpAddress,
                                     @RequestParam("clientTime")ZonedDateTime clientTime) throws IOException {
        return aiFeedbackService.askAi(image.getBytes(), clientIpAddress, clientTime);
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

    @PostMapping("/swap")
    public void swapAiFeedbacks(@RequestBody SwapAiFeedbackDto swapAiFeedbackDto) {
        aiFeedbackService.swapFeedbackOrders(swapAiFeedbackDto);
    }

    @PostMapping("/like-style")
    public AiFeedbackDto likeStyle(@RequestBody LikeStyleDto likeStyleDto) {
        return aiFeedbackService.likeStyle(likeStyleDto);
    }

    @PostMapping("/like-ai-response")
    public AiFeedbackDto likeStyle(@RequestBody LikeAiResponseDto likeAiResponseDto) {
        return aiFeedbackService.likeAiResponse(likeAiResponseDto);
    }
}
