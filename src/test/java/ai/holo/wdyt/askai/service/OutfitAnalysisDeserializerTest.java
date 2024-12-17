package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.OutfitAnalysis;
import ai.holo.wdyt.common.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OutfitAnalysisDeserializerTest {

    public static final String JSON_INPUT_1 = "{\"outfit_style\":\"casual chic, cozy, fresh\",\"style_match\":\"Perfectly suits your laid-back vibe.\",\"occasion_fit\":\"Great for brunch or casual outings with friends.\",\"trend_alert\":\"Trendy layering with warm colors.\",\"outfit_details\":[{\"item\":\"sweater\",\"color\":\"cream\",\"description\":\"Soft, oversized cardigan for a cozy touch.\"},{\"item\":\"t-shirt\",\"color\":\"white\",\"description\":\"Basic tee for classic comfort.\"},{\"item\":\"shorts\",\"color\":\"tan\",\"description\":\"Chic shorts for a relaxed vibe.\"},{\"item\":\"drink\",\"color\":\"orange\",\"description\":\"Refreshing beverage for a sunny afternoon.\"}],\"color_preference\":{\"primary\":\"cream\",\"secondary\":\"tan\"},\"enhancement_recommendations\":[\"Add a statement necklace\",\"Wear ankle boots\",\"Layer with a scarf\"],\"hair_advice\":\"Sleek ponytail\",\"coordinate_recommendations\":{\"outfit\":[{\"x\":540,\"y\":720},{\"x\":540,\"y\":780},{\"x\":540,\"y\":840},{\"x\":540,\"y\":900}],\"enhancements\":[{\"x\":460,\"y\":720},{\"x\":460,\"y\":780},{\"x\":460,\"y\":840}]},\"summary\":\"Your outfit is effortlessly stylish and perfect for a cheerful brunch. Embrace your chic, cozy vibe this festive season!\",\"compliment\":\"You look fabulous!\"}";

    @Test
    public void testJsonSerialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        OutfitAnalysis outfitAnalysis = objectMapper.readValue(JsonUtils.preprocessGptJson(JSON_INPUT_1), OutfitAnalysis.class);
        assertEquals("tan", outfitAnalysis.colorPreference().secondary());
        assertEquals(3, outfitAnalysis.enhancementRecommendations().size());
        assertEquals(4, outfitAnalysis.outfitDetails().size());
        assertEquals(4, outfitAnalysis.coordinateRecommendations().outfit().size());
        assertEquals(3, outfitAnalysis.coordinateRecommendations().enhancements().size());
    }
}
