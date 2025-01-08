package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.HeadStyleAnalysis;
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
public class HeadStyleAnalysisDeserializer extends StdDeserializer<HeadStyleAnalysis> {

    public HeadStyleAnalysisDeserializer() {
        super(HeadStyleAnalysis.class);
    }

    @Override
    public HeadStyleAnalysis deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode rootNode = parser.getCodec().readTree(parser);

        // Deserialize fields
        String headStyle = getText(rootNode, "head_style");
        String styleFaceFit = getText(rootNode, "style_face_fit");
        String occasionFit = getText(rootNode, "occasion_fit");
        String trendAlert = getText(rootNode, "trend_alert");
        String summary = getText(rootNode, "summary");
        String compliment = getText(rootNode, "compliment");
        String hairAdvice = getText(rootNode, "hair_advice");

        List<HeadStyleAnalysis.OutfitDetail> detailedElements = getDetailedElements(rootNode);
        HeadStyleAnalysis.ColorPreference colorPreference = getColorPreference(rootNode);
        List<String> enhancementRecommendations = getEnhancementRecommendations(rootNode);
        HeadStyleAnalysis.CoordinateRecommendations coordinateRecommendations = getCoordinateRecommendations(rootNode);
        Tag tag = getTag(rootNode);

        // Return final object
        return new HeadStyleAnalysis(
                headStyle,
                styleFaceFit,
                occasionFit,
                trendAlert,
                detailedElements,
                colorPreference,
                enhancementRecommendations,
                hairAdvice,
                coordinateRecommendations,
                summary,
                compliment,
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

    private List<HeadStyleAnalysis.OutfitDetail> getDetailedElements(JsonNode rootNode) {
        try {
            List<HeadStyleAnalysis.OutfitDetail> detailedElements = new ArrayList<>();
            JsonNode detailedElementsNode = rootNode.get("detailed_elements");

            // Check if detailed_elements is an array
            if (detailedElementsNode.isArray()) {
                for (JsonNode elementNode : detailedElementsNode) {
                    detailedElements.add(new HeadStyleAnalysis.OutfitDetail(
                            elementNode.get("item").asText(),
                            elementNode.get("description").asText(),
                            elementNode.get("color").asText()
                    ));
                }
            }
            return detailedElements;
        } catch (Exception ignored) {
        }
        return null;
    }

    private HeadStyleAnalysis.CoordinateRecommendations getCoordinateRecommendations(JsonNode rootNode) {
        try {
            JsonNode recommendationsNode = rootNode.get("coordinate_recommendations");

            // Parse element coordinates
            List<HeadStyleAnalysis.Coordinate> elementCoordinates = new ArrayList<>();
            JsonNode elementNode = recommendationsNode.get("elements");
            if (elementNode.isArray()) {
                for (JsonNode coordinateNode : elementNode) {
                    elementCoordinates.add(new HeadStyleAnalysis.Coordinate(
                            coordinateNode.get("x").asInt(),
                            coordinateNode.get("y").asInt()
                    ));
                }
            }

            // Parse enhancements coordinates
            List<HeadStyleAnalysis.Coordinate> enhancementsCoordinates = new ArrayList<>();
            JsonNode enhancementsNode = recommendationsNode.get("enhancements");
            if (enhancementsNode.isArray()) {
                for (JsonNode coordinateNode : enhancementsNode) {
                    enhancementsCoordinates.add(new HeadStyleAnalysis.Coordinate(
                            coordinateNode.get("x").asInt(),
                            coordinateNode.get("y").asInt()
                    ));
                }
            }

            return new HeadStyleAnalysis.CoordinateRecommendations(elementCoordinates, enhancementsCoordinates);
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

    private HeadStyleAnalysis.ColorPreference getColorPreference(JsonNode rootNode) {
        try {
            // Deserialize color_preference
            JsonNode colorPreferenceNode = rootNode.get("color_preference");
            return new HeadStyleAnalysis.ColorPreference(
                    colorPreferenceNode.get("primary").asText(),
                    colorPreferenceNode.get("secondary").asText()
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
            List<String> color = getListText(tagNode, "color");
            return new Tag(styles, occasions, color);
        } catch (Exception ignored) {
        }
        return null;
    }
}
