package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.Color;
import ai.holo.wdyt.askai.model.dto.ComparisonAnalysis;
import ai.holo.wdyt.askai.model.dto.Tag;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ComparisonAnalysisDeserializer extends StdDeserializer<ComparisonAnalysis> {

    public ComparisonAnalysisDeserializer() {
        super(ComparisonAnalysis.class);
    }

    @Override
    public ComparisonAnalysis deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode rootNode = parser.getCodec().readTree(parser);

        // Deserialize fields
        String winnerDetermination = getText(rootNode, "winnerDetermination");
        int winner = rootNode.get("winner").asInt();
        List<String> winnerCriteria = getListText(rootNode, "winnerCriteria");
        String summary = getText(rootNode, "summary");
        String areasForImprovement = getText(rootNode, "areasForImprovement");
        String finalCompliment = getText(rootNode, "finalCompliment");
        ComparisonAnalysis.EnhancementRecommendations enhancementRecommendations = getEnhancementRecommendations(rootNode);
        Tag tags = getTag(rootNode);

        // Return final object
        return new ComparisonAnalysis(
                winnerDetermination,
                winner,
                winnerCriteria,
                summary,
                enhancementRecommendations,
                areasForImprovement,
                finalCompliment,
                tags
        );
    }

    private String getText(JsonNode rootNode, String fieldName) {
        try {
            return rootNode.get(fieldName).asText();
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<String> getListText(JsonNode rootNode, String fieldName) {
        try {
            JsonNode listNode = rootNode.get(fieldName);
            if (listNode != null && listNode.isArray()) {
                List<String> list = new ArrayList<>();
                for (JsonNode node : listNode) {
                    list.add(node.asText());
                }
                return list;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private ComparisonAnalysis.EnhancementRecommendations getEnhancementRecommendations(JsonNode rootNode) {
        try {
            JsonNode enhancementRecommendationsNode = rootNode.get("enhancementRecommendations");
            return new ComparisonAnalysis.EnhancementRecommendations(
                    enhancementRecommendationsNode.get("outfit1").asText(),
                    enhancementRecommendationsNode.get("outfit2").asText()
            );
        } catch (Exception ignored) {
        }
        return null;
    }

    private Tag getTag(JsonNode rootNode) {
        try {
            JsonNode tagNode = rootNode.get("tags");
            List<String> styles = getListText(tagNode, "style");
            List<String> occasions = getListText(tagNode, "occasion");
            List<Color> color = getColor(tagNode);
            return new Tag(styles, occasions, color);
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<Color> getColor(JsonNode rootNode) {
        try {
            List<Color> colors = new ArrayList<>();
            JsonNode outfitDetailsNode = rootNode.get("color");

            if (outfitDetailsNode.isArray()) {
                for (JsonNode detailNode : outfitDetailsNode) {
                    colors.add(new Color(
                            detailNode.get("name").asText(),
                            detailNode.get("code").asText()));
                }
            }
            return colors;
        } catch (Exception ignored) {
        }
        return null;
    }
}
