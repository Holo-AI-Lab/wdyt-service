package ai.holo.wdyt.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JsonConverter<T> implements AttributeConverter<T, String> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper;
    private final Class<T> entityType;

    public JsonConverter(Class<T> entityType) {
        this.entityType = entityType;
        mapper = new ObjectMapper();
    }

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        try {
            boolean isAttributeNullOrEmptyCollection = attribute == null ||
                    (attribute instanceof Collections) && CollectionUtils.isEmpty((Collection<?>) attribute);
            if (isAttributeNullOrEmptyCollection) {
                return null;
            }

            return mapper.writer().forType(attribute.getClass()).writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize as object into JSON: {}", attribute, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        try {
            if (dbData != null) {
                if(dbData.startsWith("[")) {
                    JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, entityType);
                    return mapper.reader().forType(type).readValue(dbData);
                } else {
                    return mapper.reader().forType(entityType).readValue(dbData);
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error("Failed to deserialize as object from JSON: {}", dbData, e);
            throw new RuntimeException(e);
        }
    }
}