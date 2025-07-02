package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.service.OutfitAnalysisDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
@JsonDeserialize(using = OutfitAnalysisDeserializer.class)
public record OutfitAnalysis(
        String outfitStyle,
        String styleMatch,
        String occasionFit,
        String trendAlert,
        ColorPreference colorPreference,
        List<String> enhancementRecommendations,
        List<OutfitItemRecommendation> outfitItemRecommendations,
        String hairAdvice,
        String summary,
        Tag tag
) implements Taggable {

    @Override
    public Tag getTag() {
        return tag;
    }

    public record ColorPreference(
            Color primary,
            Color secondary,
            String comment
    ) {}

    public record OutfitItemRecommendation(
            String itemName,
            String type,
            String color,
            String season
    ) {}
}
