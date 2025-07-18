package ai.holo.wdyt.wardrobe.controller;

import ai.holo.wdyt.wardrobe.model.dto.*;
import ai.holo.wdyt.wardrobe.service.WardrobeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wardrobe")
public class WardrobeController {

    private final WardrobeService wardrobeService;

    public WardrobeController(WardrobeService wardrobeService) {
        this.wardrobeService = wardrobeService;
    }

    @GetMapping("/")
    public Page<WardrobeItemDto> listWardrobeItems(@RequestParam(required = false) String category,
                                                   @RequestParam(defaultValue = "20") Integer size,
                                                   @RequestParam(defaultValue = "0") Integer page) {
        return wardrobeService.listWardrobeItems(category, PageRequest.of(page, size));
    }

    @GetMapping("/item/{id}")
    public WardrobeItemDto getItemById(@PathVariable("id") Long id) {
        return wardrobeService.getItemById(id);
    }

    @PostMapping("/filter")
    public Page<WardrobeItemDto> filterItems(@RequestBody WardrobeItemFilterRequest filterRequest,
                                             @RequestParam(defaultValue = "20") Integer size,
                                             @RequestParam(defaultValue = "0") Integer page) {
        Pageable pageable = PageRequest.of(page, size);
        return wardrobeService.filter(filterRequest, pageable);
    }

    @PostMapping("/add-item")
    public WardrobeItemDto createItem(@RequestBody CreateWardrobeItemDto dto) {
        return wardrobeService.createItem(dto);
    }

    @PostMapping("/update-item/{id}")
    public WardrobeItemDto updateItem(@PathVariable("id") Long id,
                                      @RequestBody UpdateWardrobeItemDto dto) {
        return wardrobeService.updateItem(id, dto);
    }

    @DeleteMapping("/delete-item/{id}")
    public void deleteItem(@PathVariable("id") Long id) {
        wardrobeService.deleteItem(id);
    }

    @PostMapping("/like-item/{id}")
    public WardrobeItemDto likeItem(@PathVariable("id") Long id,
                                    @RequestParam boolean like) {
        return wardrobeService.likeItem(id, like);
    }

    @PostMapping("/report")
    public void reportItem(@RequestBody WardrobeReportRequest request) {
        wardrobeService.reportItem(request);
    }
}
