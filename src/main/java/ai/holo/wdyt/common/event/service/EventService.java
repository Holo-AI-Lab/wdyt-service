package ai.holo.wdyt.common.event.service;

import ai.holo.wdyt.common.event.Event;
import ai.holo.wdyt.common.event.model.EventLog;
import ai.holo.wdyt.common.event.repository.EventLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class EventService {
    private final EventLogRepository eventLogRepository;
    private final EventSerializer eventSerializer;
    private final SecurityContextAware securityContextAware;

    public EventService(EventLogRepository eventLogRepository,
                        EventSerializer eventSerializer,
                        SecurityContextAware securityContextAware) {
        this.eventLogRepository = eventLogRepository;
        this.eventSerializer = eventSerializer;
        this.securityContextAware = securityContextAware;
    }

    @Transactional
    public void saveEventLog(Event event) {
        UUID eventId = UUID.randomUUID();
        event.setEventId(eventId);
        String jsonPayload = mapEventToJson(event);
        Long producedByAccountId = securityContextAware.getLoggedInUserId();

        EventLog eventLog = new EventLog(eventId.toString(), event.getEventName(), jsonPayload, LocalDateTime.now(), producedByAccountId, 0);
        eventLogRepository.save(eventLog);
    }

    private String mapEventToJson(Event event) {
        try {
            return eventSerializer.serialize(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error on serializing event to JSON!", e);
        }
    }

    @Transactional
    public void deleteEventLog(Event event) {
        EventLog eventLog = eventLogRepository.findById(event.getEventId().toString()).orElseGet(() -> {
            String errorMessage = "Event cannot be found on Event Log table. It may have already been consumed!";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        });
        eventLogRepository.delete(eventLog);
    }
}
