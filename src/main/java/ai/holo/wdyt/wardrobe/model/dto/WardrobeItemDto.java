package ai.holo.wdyt.wardrobe.model.dto;

import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItem;
import ai.holo.wdyt.wardrobe.model.entity.Tags;

public record WardrobeItemDto(
        Long id,
        String name,
        String imagePath,
        WardrobeItemCategory category,
        boolean liked,
        Tags tags
) {
    public WardrobeItemDto(WardrobeItem wardrobeItem){
        this(
                wardrobeItem.getId(),
                wardrobeItem.getName(),
                wardrobeItem.getImagePath(),
                wardrobeItem.getCategory(),
                wardrobeItem.isLiked(),
                wardrobeItem.getTags()
        );
    }
}
