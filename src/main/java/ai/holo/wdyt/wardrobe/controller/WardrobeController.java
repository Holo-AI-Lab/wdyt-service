package ai.holo.wdyt.wardrobe.controller;

import ai.holo.wdyt.wardrobe.model.dto.*;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;
import ai.holo.wdyt.wardrobe.service.WardrobeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wardrobe")
public class WardrobeController {

    private final WardrobeService wardrobeService;

    public WardrobeController(WardrobeService wardrobeService) {
        this.wardrobeService = wardrobeService;
    }

    @GetMapping("/")
    public Page<WardrobeItemDto> listWardrobeItems(@RequestParam(required = false) WardrobeItemCategory category,
                                                   @RequestParam(required = false) List<String> colors,
                                                   @RequestParam(required = false) List<String> seasons,
                                                   @RequestParam(required = false) List<String> types,
                                                   @RequestParam(required = false) Boolean liked,
                                                   @RequestParam(defaultValue = "20") Integer size,
                                                   @RequestParam(defaultValue = "0") Integer page) {
        return wardrobeService.listWardrobeItems(category, liked, colors, seasons, types, PageRequest.of(page, size));
    }

    @GetMapping("/item/{id}")
    public WardrobeItemDto getItemById(@PathVariable("id") Long id) {
        return wardrobeService.getItemById(id);
    }

    @GetMapping("/filter")
    public FilterDto filter() {
        return wardrobeService.filter();
    }

    @PostMapping("/add-item")
    public List<WardrobeItemDto> createItem(@RequestBody CreateWardrobeItemsRequest dto) {
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

    @GetMapping("/subcategories")
    public List<String> getSubcategories(@RequestParam String category,
                                         @RequestParam(required = false) String search) {
        return wardrobeService.getSubcategories(category, search);
    }
}
