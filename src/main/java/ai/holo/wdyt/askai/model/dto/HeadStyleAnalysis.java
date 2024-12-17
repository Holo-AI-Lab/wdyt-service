package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.service.HeadStyleAnalysisDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(using = HeadStyleAnalysisDeserializer.class)
public record HeadStyleAnalysis(
        String outfitStyle,
        String styleMatch,
        String occasionFit,
        String trendAlert,
        List<OutfitDetail> outfitDetails,
        ColorPreference colorPreference,
        List<String> enhancementRecommendations,
        String hairAdvice,
        CoordinateRecommendations coordinateRecommendations,
        String summary,
        String compliment
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

}
