package ai.holo.wdyt.wardrobe.model.dto;

import java.util.List;

public record DraftWardrobeItemsDto(
        String name,
        String content,
        String label,
        String color,
        List<String> colorCode,
        String season,
        String imageUrl
) {
}