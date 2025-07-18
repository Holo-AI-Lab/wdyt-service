package ai.holo.wdyt.wardrobe.model.dto;

import ai.holo.wdyt.askai.model.dto.Color;
import ai.holo.wdyt.wardrobe.model.entity.Category;

import java.util.List;

public record CreateWardrobeItemDto(
        String name,
        String imagePath,
        Category category,
        boolean liked,
        List<Color> colors,
        List<String> seasons,
        List<String> types,
        List<String> tags
) {
}
