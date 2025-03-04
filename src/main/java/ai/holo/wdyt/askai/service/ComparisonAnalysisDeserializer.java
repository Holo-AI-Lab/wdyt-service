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
        String outfitStyles = getText(rootNode, "outfitStyles");
        ComparisonAnalysis.StyleMatch styleMatch = getStyleMatch(rootNode);
        ComparisonAnalysis.OccasionFit occasionFit = getOccasionFit(rootNode);
        ComparisonAnalysis.TrendAlert trendAlert = getTrendAlert(rootNode);
        ComparisonAnalysis.ColorPreference colorPreference = getColorPreference(rootNode);
        ComparisonAnalysis.EnhancementRecommendations enhancementRecommendations = getEnhancementRecommendations(rootNode);
        ComparisonAnalysis.HairAdvice hairAdvice = getHairAdvice(rootNode);
        String winnerDetermination = getText(rootNode, "winnerDetermination");
        String summary = getText(rootNode, "summary");
        String finalCompliment = getText(rootNode, "finalCompliment");
        int winner = rootNode.get("winner").asInt();
        Tag tags = getTags(rootNode);

        // Return final object
        return new ComparisonAnalysis(
                outfitStyles,
                styleMatch,
                occasionFit,
                trendAlert,
                colorPreference,
                enhancementRecommendations,
                hairAdvice,
                winnerDetermination,
                summary,
                finalCompliment,
                winner,
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

    private ComparisonAnalysis.StyleMatch getStyleMatch(JsonNode rootNode) {
        try {
            JsonNode styleMatchNode = rootNode.get("styleMatch");
            return new ComparisonAnalysis.StyleMatch(
                    styleMatchNode.get("outfit1").asText(),
                    styleMatchNode.get("outfit2").asText()
            );
        } catch (Exception ignored) {
        }
        return null;
    }

    private ComparisonAnalysis.OccasionFit getOccasionFit(JsonNode rootNode) {
        try {
            JsonNode occasionFitNode = rootNode.get("occasionFit");
            return new ComparisonAnalysis.OccasionFit(
                    occasionFitNode.get("outfit1").asText(),
                    occasionFitNode.get("outfit2").asText()
            );
        } catch (Exception ignored) {
        }
        return null;
    }

    private ComparisonAnalysis.TrendAlert getTrendAlert(JsonNode rootNode) {
        try {
            JsonNode trendAlertNode = rootNode.get("trendAlert");
            return new ComparisonAnalysis.TrendAlert(
                    trendAlertNode.get("outfit1").asText(),
                    trendAlertNode.get("outfit2").asText()
            );
        } catch (Exception ignored) {
        }
        return null;
    }

    private ComparisonAnalysis.ColorPreference getColorPreference(JsonNode rootNode) {
        try {
            JsonNode colorPreferenceNode = rootNode.get("colorPreference");
            return new ComparisonAnalysis.ColorPreference(
                    colorPreferenceNode.get("outfit1").asText(),
                    colorPreferenceNode.get("outfit2").asText()
            );
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

    private ComparisonAnalysis.HairAdvice getHairAdvice(JsonNode rootNode) {
        try {
            JsonNode hairAdviceNode = rootNode.get("hairAdvice");
            return new ComparisonAnalysis.HairAdvice(
                    hairAdviceNode.get("outfit1").asText(),
                    hairAdviceNode.get("outfit2").asText()
            );
        } catch (Exception ignored) {
        }
        return null;
    }

    private Tag getTags(JsonNode rootNode) {
        try {
            JsonNode tagsNode = rootNode.get("tags");
            List<String> style = getListText(tagsNode, "style");
            List<String> occasion = getListText(tagsNode, "occasion");
            List<Color> color = getColor(tagsNode);
            return new Tag(style, occasion, color);
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<String> getListText(JsonNode rootNode, String fieldName) {
        try {
            JsonNode listNode = rootNode.get(fieldName);
            if (listNode.isArray()) {
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