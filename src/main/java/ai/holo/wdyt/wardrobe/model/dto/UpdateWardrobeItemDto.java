package ai.holo.wdyt.wardrobe.model.dto;

import ai.holo.wdyt.wardrobe.model.entity.Color;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;

import java.util.List;

public record UpdateWardrobeItemDto(
        String name,
        WardrobeItemCategory category,
        boolean liked,
        List<Color> colors,
        List<String> seasons,
        List<String> types,
        List<String> tags
) {
}
