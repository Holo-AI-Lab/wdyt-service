package ai.holo.wdyt.wardrobe.controller;

import ai.holo.wdyt.wardrobe.model.dto.DraftWardrobeItemsDto;
import ai.holo.wdyt.wardrobe.model.dto.WardrobeAutoExtractRequestDto;
import ai.holo.wdyt.wardrobe.service.WardrobeItemAutoExtractService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wardrobe-extract")
public class WardrobeItemExtractionController {

    private final WardrobeItemAutoExtractService wardrobeItemAutoExtractService;

    public WardrobeItemExtractionController(WardrobeItemAutoExtractService wardrobeItemAutoExtractService) {
        this.wardrobeItemAutoExtractService = wardrobeItemAutoExtractService;
    }

    @PostMapping("/auto-extract")
    public List<DraftWardrobeItemsDto> updateUserInfo(@RequestBody WardrobeAutoExtractRequestDto wardrobeAutoExtractRequestDto) {
        return wardrobeItemAutoExtractService.extractWardrobeItems(wardrobeAutoExtractRequestDto.imageUrl());
    }
}
