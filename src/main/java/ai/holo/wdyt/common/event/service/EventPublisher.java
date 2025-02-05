package ai.holo.wdyt.common.event.service;

import ai.holo.wdyt.common.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EventService eventService;
    private final EventContext eventContext;

    public EventPublisher(ApplicationEventPublisher applicationEventPublisher,
                          EventService eventService, EventContext eventContext) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.eventService = eventService;
        this.eventContext = eventContext;
    }

    @Transactional
    public void publishEvent(Event event) {
        // if the same event with same the parameters is produced within the transaction
        // the newly incoming event will be ignored silently and it will not be published.
        if (eventContext.containsEvent(event)) {
            log.debug(String.format("Same event with same parameters has already been published within the transaction. This instance of %s will be ignored!", event.getEventName()));
            return;
        }

        if (event.consistencyMechanismEnabled()) {
            eventService.saveEventLog(event);
        }
        eventContext.addEvent(event);
        applicationEventPublisher.publishEvent(event);
    }
}
