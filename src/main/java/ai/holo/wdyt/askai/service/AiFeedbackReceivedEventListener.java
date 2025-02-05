package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.event.AiFeedbackReceivedEvent;
import ai.holo.wdyt.common.event.service.EventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@EventConsumer
@Async
@Slf4j
public class AiFeedbackReceivedEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAiFeedbackReceivedEvent(AiFeedbackReceivedEvent event) {
        // handle event
        log.info("Handling AiFeedbackReceivedEvent: {}", event.getAiFeedbackId());
    }

}
