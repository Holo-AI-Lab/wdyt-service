package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.HeadStyleAnalysis;
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
        String outfitStyle = getText(rootNode, "head_style");
        String styleMatch = getText(rootNode, "style_face_fit");
        String occasionFit = getText(rootNode, "occasion_fit");
        String trendAlert = getText(rootNode, "trend_alert");
        String summary = getText(rootNode, "summary");
        String compliment = getText(rootNode, "compliment");
        String hairAdvice = getText(rootNode, "hair_advice");

        List<HeadStyleAnalysis.OutfitDetail> outfitDetails = getOutfitDetails(rootNode);
        HeadStyleAnalysis.ColorPreference colorPreference = getColorPreference(rootNode);
        List<String> enhancementRecommendations = getEnhancementRecommendations(rootNode);
        HeadStyleAnalysis.CoordinateRecommendations coordinateRecommendations = getCoordinateRecommendations(rootNode);

        // Return final object
        return new HeadStyleAnalysis(
                outfitStyle,
                styleMatch,
                occasionFit,
                trendAlert,
                outfitDetails,
                colorPreference,
                enhancementRecommendations,
                hairAdvice,
                coordinateRecommendations,
                summary,
                compliment
        );
    }

    private String getText(JsonNode rootNode, String fieldName) {
        try {
            return rootNode.get(fieldName).asText();
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<HeadStyleAnalysis.OutfitDetail> getOutfitDetails(JsonNode rootNode) {
        try {
            List<HeadStyleAnalysis.OutfitDetail> outfitDetails = new ArrayList<>();
            JsonNode outfitDetailsNode = rootNode.get("detailed_elements");

            // Check if detailed_elements is an array
            if (outfitDetailsNode.isArray()) {
                for (JsonNode detailNode : outfitDetailsNode) {
                    outfitDetails.add(new HeadStyleAnalysis.OutfitDetail(
                            detailNode.get("item").asText(),
                            detailNode.get("color").asText(),
                            detailNode.get("description").asText()
                    ));
                }
            }
            return outfitDetails;
        } catch (Exception ignored) {
        }
        return null;
    }

    private HeadStyleAnalysis.CoordinateRecommendations getCoordinateRecommendations(JsonNode rootNode) {
        try {
            JsonNode recommendationsNode = rootNode.get("coordinate_recommendations");

            // Parse outfit coordinates
            List<HeadStyleAnalysis.Coordinate> outfitCoordinates = new ArrayList<>();
            JsonNode outfitNode = recommendationsNode.get("elements");
            if (outfitNode.isArray()) {
                for (JsonNode coordinateNode : outfitNode) {
                    outfitCoordinates.add(new HeadStyleAnalysis.Coordinate(
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

            return new HeadStyleAnalysis.CoordinateRecommendations(outfitCoordinates, enhancementsCoordinates);
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

}
