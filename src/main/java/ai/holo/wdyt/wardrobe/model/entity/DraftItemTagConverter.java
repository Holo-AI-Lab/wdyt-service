package ai.holo.wdyt.wardrobe.model.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class DraftItemTagConverter implements AttributeConverter<List<DraftItemTag>, String> {

    private final ObjectMapper objectMapper;

    public DraftItemTagConverter() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String convertToDatabaseColumn(List<DraftItemTag> draftItemTags) {
        if (draftItemTags == null || draftItemTags.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(draftItemTags);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting list to JSON", e);
        }
    }

    @Override
    public List<DraftItemTag> convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, DraftItemTag.class));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to list", e);
        }
    }
}