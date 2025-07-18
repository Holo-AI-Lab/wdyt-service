package ai.holo.wdyt.wardrobe.model.dto;

import java.util.List;

public record WardrobeItemFilterRequest(
        List<String> colors,
        List<String> seasons,
        List<String> types,
        boolean liked
) {}
