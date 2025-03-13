package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.service.ComparisonAnalysisDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ComparisonAnalysisDeserializer.class)
public record ComparisonAnalysis(
        String outfitStyles,
        StyleMatch styleMatch,
        OccasionFit occasionFit,
        TrendAlert trendAlert,
        ComparisonColorPreference colorPreference,
        EnhancementRecommendations enhancementRecommendations,
        HairAdvice hairAdvice,
        String winnerDetermination,
        String summary,
        String finalCompliment,
        int winner,
        Tag tag
) implements Taggable {

    @Override
    public Tag getTag() {
        return tag;
    }

    public record StyleMatch(
            String outfit1,
            String outfit2
    ) {}

    public record OccasionFit(
            String outfit1,
            String outfit2
    ) {}

    public record TrendAlert(
            String outfit1,
            String outfit2
    ) {}

    public record ComparisonColorPreference(
            String outfit1,
            String outfit2
    ) {}

    public record EnhancementRecommendations(
            String outfit1,
            String outfit2
    ) {}

    public record HairAdvice(
            String outfit1,
            String outfit2
    ) {}
}