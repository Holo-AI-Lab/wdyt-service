package ai.holo.wdyt.wardrobe.model.entity;

import ai.holo.wdyt.askai.model.entity.FeedbackEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class ColorConverter implements AttributeConverter<List<Color>, String> {

    private final ObjectMapper objectMapper;

    public ColorConverter() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String convertToDatabaseColumn(List<Color> feedbackEntries) {
        if (feedbackEntries == null || feedbackEntries.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(feedbackEntries);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting list to JSON", e);
        }
    }

    @Override
    public List<Color> convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Color.class));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to list", e);
        }
    }
}