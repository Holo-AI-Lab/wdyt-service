package ai.holo.wdyt.wardrobe.model.dto;

import ai.holo.wdyt.wardrobe.model.entity.*;

import java.util.List;

public record DraftWardrobeItemDto(
        Long id,
        String name,
        WardrobeItemCategory category,
        List<String> types,
        List<String> seasons,
        List<Color> colors,
        String imageUrl,
        List<String> tags
) {

    public DraftWardrobeItemDto(DraftWardrobeItem item, String imageUrl) {
        this(item.getId(), item.getName(), item.getCategory(), item.getSubCategories().stream().map(SubCategory::name).toList(),
                item.getSeasons().stream().map(Season::name).toList(), item.getColors(), imageUrl,
                item.getTags().stream().map(DraftItemTag::name).toList());
    }
}