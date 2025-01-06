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
        List<OutfitDetail> outfitDetails,
        ColorPreference colorPreference,
        List<String> enhancementRecommendations,
        String hairAdvice,
        CoordinateRecommendations coordinateRecommendations,
        Summary summary,
        String upliftingCompliment
) {

    public record OutfitDetail(
            String item,
            String color,
            String description
    ) {}

    public record ColorPreference(
            String primary,
            String secondary
    ) {}

    public record CoordinateRecommendations(
            List<Coordinate> outfit,
            List<Coordinate> enhancements
    ) {}

    public record Coordinate(int x, int y) {}

    public record Summary(
            String impression,
            String suitability,
            String personalReflection,
            List<String> enhancements,
            String compliment
    ) {}

}
