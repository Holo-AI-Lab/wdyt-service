package ai.holo.wdyt.wardrobe.model.dto;

import ai.holo.wdyt.wardrobe.model.entity.Color;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;

import java.util.List;

public record FilterDto(
        List<WardrobeItemCategory> categories,
        List<Color> colors,
        List<String> seasons,
        List<String> types
) {
}
