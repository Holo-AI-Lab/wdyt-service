package ai.holo.wdyt.wardrobe.controller;

import ai.holo.wdyt.wardrobe.model.dto.DraftWardrobeItemDto;
import ai.holo.wdyt.wardrobe.model.dto.WardrobeAutoExtractRequestDto;
import ai.holo.wdyt.wardrobe.model.dto.WardrobeManualExtractDto;
import ai.holo.wdyt.wardrobe.model.dto.WardrobeManualExtractRequestDataDto;
import ai.holo.wdyt.wardrobe.service.WardrobeItemAutoExtractService;
import ai.holo.wdyt.wardrobe.service.WardrobeItemManualExtractService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wardrobe-extract")
public class WardrobeItemExtractionController {

    private final WardrobeItemAutoExtractService wardrobeItemAutoExtractService;
    private final WardrobeItemManualExtractService wardrobeItemManualExtractService;

    public WardrobeItemExtractionController(WardrobeItemAutoExtractService wardrobeItemAutoExtractService,
                                            WardrobeItemManualExtractService wardrobeItemManualExtractService) {
        this.wardrobeItemAutoExtractService = wardrobeItemAutoExtractService;
        this.wardrobeItemManualExtractService = wardrobeItemManualExtractService;
    }

    @PostMapping("/auto-extract")
    public List<DraftWardrobeItemDto> updateUserInfo(@RequestBody @Valid WardrobeAutoExtractRequestDto wardrobeAutoExtractRequestDto) {
        return wardrobeItemAutoExtractService.extractWardrobeItems(wardrobeAutoExtractRequestDto.aiFeedbackId());
    }

    @PostMapping(value = "/manual-extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DraftWardrobeItemDto submitImage(@RequestPart(value = "image") MultipartFile image,
                                            @RequestPart("data") String data) throws IOException {
        byte[] imageBytes = image != null ? image.getBytes() : null;
        WardrobeManualExtractDto manualExtractRequestDto = wardrobeItemManualExtractService.validateAndParseManualExtractDto(imageBytes, data);
        String imagePath = wardrobeItemManualExtractService.prepareImageForManualExtraction(manualExtractRequestDto);
        return wardrobeItemManualExtractService.extractWardrobeItems(imagePath);
    }
}
