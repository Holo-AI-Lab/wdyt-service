package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.service.ComparisonAnalysisDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(using = ComparisonAnalysisDeserializer.class)
public record ComparisonAnalysis(
        String winnerDetermination,
        int winner,
        List<String> winnerCriteria,
        String summary,
        List<String> enhancementRecommendations,
        String areasForImprovement,
        String finalCompliment,
        Tag tag
) implements Taggable {

    @Override
    public Tag getTag() {
        return tag;
    }
}