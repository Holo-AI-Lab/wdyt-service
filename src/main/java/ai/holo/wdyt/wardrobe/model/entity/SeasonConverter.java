package ai.holo.wdyt.wardrobe.model.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class SeasonConverter implements AttributeConverter<List<Season>, String> {

    private final ObjectMapper objectMapper;

    public SeasonConverter() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String convertToDatabaseColumn(List<Season> seasons) {
        if (seasons == null || seasons.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(seasons);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting list to JSON", e);
        }
    }

    @Override
    public List<Season> convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Season.class));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to list", e);
        }
    }
}