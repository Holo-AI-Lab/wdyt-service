package ai.holo.wdyt.common.event.service;

import ai.holo.wdyt.common.event.Event;
import ai.holo.wdyt.config.transaction.TransactionScopeConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = TransactionScopeConfig.TRANSACTION_SCOPE_NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EventContext {
    private List<Event> producedEvents;

    public void addEvent(Event event) {
        producedEvents.add(event);
    }

    public boolean containsEvent(Event event) {
        return producedEvents.contains(event);
    }

    @PostConstruct
    public void init() {
        producedEvents = new ArrayList<>();
    }
}
