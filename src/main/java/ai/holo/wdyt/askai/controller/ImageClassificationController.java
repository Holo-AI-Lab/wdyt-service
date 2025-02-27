package ai.holo.wdyt.askai.controller;

import ai.holo.wdyt.askai.model.dto.ImageClassificationDto;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.askai.service.ImageClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/image-classification")
public class ImageClassificationController {
    private final ImageClassificationService imageClassificationService;

    @Autowired
    public ImageClassificationController(ImageClassificationService imageClassificationService) {
        this.imageClassificationService = imageClassificationService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ImageClassificationDto classifyImage(@RequestParam("image") MultipartFile image) throws IOException {
        byte[] imageBytes = image != null ? image.getBytes() : null;
        ImageType imageType = imageClassificationService.classifyImage(imageBytes);
        return new ImageClassificationDto(imageType.name());
    }
}