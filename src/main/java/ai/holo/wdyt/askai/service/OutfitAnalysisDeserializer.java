package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.Color;
import ai.holo.wdyt.askai.model.dto.OutfitAnalysis;
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
public class OutfitAnalysisDeserializer extends StdDeserializer<OutfitAnalysis> {

    public OutfitAnalysisDeserializer() {
        super(OutfitAnalysis.class);
    }

    @Override
    public OutfitAnalysis deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode rootNode = parser.getCodec().readTree(parser);

        // Deserialize fields
        String outfitStyle = getText(rootNode, "outfit_style");
        String styleMatch = getText(rootNode, "style_match");
        String occasionFit = getText(rootNode, "occasion_fit");
        String trendAlert = getText(rootNode, "trend_alert");
        String hairAdvice = getText(rootNode, "hair_advice");
        OutfitAnalysis.ColorPreference colorPreference = getColorPreference(rootNode);
        List<String> enhancementRecommendations = getEnhancementRecommendations(rootNode);
        List<OutfitAnalysis.OutfitItemRecommendation> outfitItemRecommendations = getOutfitItemRecommendations(rootNode);
        String summary = getText(rootNode, "summary");
        Tag tag = getTag(rootNode);

        // Return final object
        return new OutfitAnalysis(
                outfitStyle,
                styleMatch,
                occasionFit,
                trendAlert,
                colorPreference,
                enhancementRecommendations,
                outfitItemRecommendations,
                hairAdvice,
                summary,
                tag
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

    private List<OutfitAnalysis.OutfitItemRecommendation> getOutfitItemRecommendations(JsonNode rootNode) {
        try {
            // Deserialize outfit_item_recommendations which is an array of strings
            List<OutfitAnalysis.OutfitItemRecommendation> outfitItemRecommendations = new ArrayList<>();
            JsonNode outfitItemRecommendationsNode = rootNode.get("outfit_item_recommendations");
            // Check if the node is an array and process each element
            if (outfitItemRecommendationsNode.isArray()) {
                for (JsonNode node : outfitItemRecommendationsNode) {
                    outfitItemRecommendations.add(new OutfitAnalysis.OutfitItemRecommendation(
                            node.get("item_name").asText(),
                            node.get("type").asText(),
                            node.get("color").asText(),
                            node.get("season").asText()
                    ));
                }
            }
            return outfitItemRecommendations;
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<String> getEnhancementRecommendations(JsonNode rootNode) {
        try {
            // Deserialize enhancement_recommendations which is an array of strings
            List<String> enhancementRecommendations = new ArrayList<>();
            JsonNode enhancementNode = rootNode.get("enhancement_recommendations");
            // Check if the node is an array and process each element
            if (enhancementNode.isArray()) {
                for (JsonNode node : enhancementNode) {
                    enhancementRecommendations.add(node.asText());
                }
            }
            return enhancementRecommendations;
        } catch (Exception ignored) {
        }
        return null;
    }

    private OutfitAnalysis.ColorPreference getColorPreference(JsonNode rootNode) {
        try {
            JsonNode colorPrefNode = rootNode.get("color_preference");
            JsonNode primaryNode = colorPrefNode.get("primary");
            JsonNode secondaryNode = colorPrefNode.get("secondary");
            String comment = colorPrefNode.has("comment") ? colorPrefNode.get("comment").asText() : null;

            Color primary = new Color(
                    primaryNode.get("name").asText(),
                    primaryNode.get("code").asText()
            );

            Color secondary = new Color(
                    secondaryNode.get("name").asText(),
                    secondaryNode.get("code").asText()
            );

            return new OutfitAnalysis.ColorPreference(primary, secondary, comment);
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
}