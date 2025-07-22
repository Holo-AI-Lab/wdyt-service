package ai.holo.wdyt.wardrobe.model.dto;

import ai.holo.wdyt.wardrobe.model.entity.Color;
import ai.holo.wdyt.wardrobe.model.entity.DraftWardrobeItem;
import ai.holo.wdyt.wardrobe.model.entity.Season;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;

import java.util.List;

public record DraftWardrobeItemDto(
        Long id,
        String name,
        WardrobeItemCategory category,
        List<String> seasons,
        List<Color> colors,
        String imageUrl
) {

    public DraftWardrobeItemDto(DraftWardrobeItem item, String imageUrl) {
        this(item.getId(), item.getName(), item.getCategory(),
                item.getSeasons().stream().map(Season::name).toList(), item.getColors(), imageUrl);
    }
}