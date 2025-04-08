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

    public ComparisonAnalysis(ComparisonAnalysis comparisonAnalysis, List<String> occasions) {
        this(
                comparisonAnalysis.winnerDetermination,
                comparisonAnalysis.winner,
                comparisonAnalysis.winnerCriteria,
                comparisonAnalysis.summary,
                comparisonAnalysis.enhancementRecommendations,
                comparisonAnalysis.areasForImprovement,
                comparisonAnalysis.finalCompliment,
                new Tag(comparisonAnalysis.tag.style(), occasions, comparisonAnalysis.tag.color())
        );
    }

    @Override
    public Tag getTag() {
        return tag;
    }
}