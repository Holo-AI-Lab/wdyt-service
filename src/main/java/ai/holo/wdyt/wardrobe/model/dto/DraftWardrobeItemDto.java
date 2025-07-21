package ai.holo.wdyt.wardrobe.model.dto;

import ai.holo.wdyt.wardrobe.model.entity.Color;
import ai.holo.wdyt.wardrobe.model.entity.DraftWardrobeItem;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;

import java.util.List;

public record DraftWardrobeItemDto(
        Long id,
        String name,
        WardrobeItemCategory category,
        String season,
        List<Color> colors,
        String imageUrl
) {

    public DraftWardrobeItemDto(DraftWardrobeItem item, String imageUrl) {
        this(item.getId(), item.getName(), item.getCategory(), item.getSeason(), item.getColors(), imageUrl);
    }
}