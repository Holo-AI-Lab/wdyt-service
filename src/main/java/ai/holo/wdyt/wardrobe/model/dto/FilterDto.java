package ai.holo.wdyt.wardrobe.model.dto;

import ai.holo.wdyt.wardrobe.model.entity.Color;
import java.util.List;

public record FilterDto(
        List<Color> colors,
        List<String> seasons,
        List<String> types
) {
}
