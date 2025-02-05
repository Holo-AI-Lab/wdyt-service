package ai.holo.wdyt.common.event.service;

import ai.holo.wdyt.common.event.Event;
import ai.holo.wdyt.common.event.model.EventLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class EventSerializer {

    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();

        // This module is used to deserialize json strings for Classes which don't have default constructors Used by
        // Fallback event mechanism.
        // Please note; It doesn't work when the parameterized constructor has only one argument because of the bug:
        // FasterXML/jackson-module-parameter-names#38
        // To be on the safe side it is good to add a default constructor to Event classes
        objectMapper.registerModule(new ParameterNamesModule());
    }

    public String serialize(Event event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    public Event deserialize(EventLog eventLog) throws ClassNotFoundException, JsonProcessingException {
        return (Event) objectMapper.readValue(eventLog.getPayload(), Class.forName(eventLog.getEvent()));
    }

    /**
     * Serializes the provided object into a JSON string using Jackson's {@link ObjectMapper}.
     *
     * @param event The object to be serialized into JSON. Must be serializable by Jackson's {@link ObjectMapper}.
     * @return A JSON representation of the provided object.
     * @throws JsonProcessingException If there is a problem processing the JSON serialization.
     */
    public String serialize(Object event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }
}
