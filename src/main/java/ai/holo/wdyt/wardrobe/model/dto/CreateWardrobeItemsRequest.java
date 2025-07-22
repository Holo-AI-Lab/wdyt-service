package ai.holo.wdyt.wardrobe.model.dto;

import java.util.List;

public record CreateWardrobeItemsRequest(
        List<CreateWardrobeItemDto> items
) {}