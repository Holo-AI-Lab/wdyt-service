package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.OutfitAnalysis;
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
        List<OutfitAnalysis.OutfitDetail> outfitDetails = getOutfitDetails(rootNode);
        OutfitAnalysis.ColorPreference colorPreference = getColorPreference(rootNode);
        List<String> enhancementRecommendations = getEnhancementRecommendations(rootNode);
        OutfitAnalysis.CoordinateRecommendations coordinateRecommendations = getCoordinateRecommendations(rootNode);
        String upliftingCompliment = getText(rootNode, "uplifting_compliment");

        // Handle the summary field (nested object)
        OutfitAnalysis.Summary summary = getSummary(rootNode);

        // Return final object
        return new OutfitAnalysis(
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
                upliftingCompliment
        );
    }

    private String getText(JsonNode rootNode, String fieldName) {
        try {
            return rootNode.get(fieldName).asText();
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<OutfitAnalysis.OutfitDetail> getOutfitDetails(JsonNode rootNode) {
        try {
            List<OutfitAnalysis.OutfitDetail> outfitDetails = new ArrayList<>();
            JsonNode outfitDetailsNode = rootNode.get("outfit_details");

            // Check if outfit_details is an array
            if (outfitDetailsNode.isArray()) {
                for (JsonNode detailNode : outfitDetailsNode) {
                    outfitDetails.add(new OutfitAnalysis.OutfitDetail(
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

    private OutfitAnalysis.CoordinateRecommendations getCoordinateRecommendations(JsonNode rootNode) {
        try {
            JsonNode recommendationsNode = rootNode.get("coordinate_recommendations");

            // Parse outfit coordinates
            List<OutfitAnalysis.Coordinate> outfitCoordinates = new ArrayList<>();
            JsonNode outfitNode = recommendationsNode.get("outfit");
            if (outfitNode.isArray()) {
                for (JsonNode coordinateNode : outfitNode) {
                    outfitCoordinates.add(new OutfitAnalysis.Coordinate(
                            coordinateNode.get("x").asInt(),
                            coordinateNode.get("y").asInt()
                    ));
                }
            }

            // Parse enhancements coordinates
            List<OutfitAnalysis.Coordinate> enhancementsCoordinates = new ArrayList<>();
            JsonNode enhancementsNode = recommendationsNode.get("enhancements");
            if (enhancementsNode.isArray()) {
                for (JsonNode coordinateNode : enhancementsNode) {
                    enhancementsCoordinates.add(new OutfitAnalysis.Coordinate(
                            coordinateNode.get("x").asInt(),
                            coordinateNode.get("y").asInt()
                    ));
                }
            }

            return new OutfitAnalysis.CoordinateRecommendations(outfitCoordinates, enhancementsCoordinates);
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
            // Deserialize color_preference
            JsonNode colorPreferenceNode = rootNode.get("color_preference");
            return new OutfitAnalysis.ColorPreference(
                    colorPreferenceNode.get("primary").asText(),
                    colorPreferenceNode.get("secondary").asText()
            );
        } catch (Exception ignored) {
        }
        return null;
    }

    // New method to handle the "summary" field (nested object)
    private OutfitAnalysis.Summary getSummary(JsonNode rootNode) {
        try {
            JsonNode summaryNode = rootNode.get("summary");
            return new OutfitAnalysis.Summary(
                    summaryNode.get("impression").asText(),
                    summaryNode.get("suitability").asText(),
                    summaryNode.get("personal_reflection").asText(),
                    getEnhancementsList(summaryNode),
                    summaryNode.get("compliment").asText()
            );
        } catch (Exception ignored) {
        }
        return null;
    }

    // Helper method to parse the enhancements array inside the summary object
    private List<String> getEnhancementsList(JsonNode summaryNode) {
        try {
            List<String> enhancements = new ArrayList<>();
            JsonNode enhancementsNode = summaryNode.get("enhancements");
            if (enhancementsNode.isArray()) {
                for (JsonNode enhancement : enhancementsNode) {
                    enhancements.add(enhancement.asText());
                }
            }
            return enhancements;
        } catch (Exception ignored) {
        }
        return null;
    }
}