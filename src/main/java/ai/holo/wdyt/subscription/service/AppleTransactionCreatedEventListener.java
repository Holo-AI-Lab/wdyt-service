package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.subscription.model.event.AppleTransactionCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@EventConsumer
@Async
@Slf4j
public class AppleTransactionCreatedEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionSavedEvent(AppleTransactionCreatedEvent event) {
        // handle event
        log.info("Handling Apple Transaction Created Event: {}", event.getAppleTransactionId());
        // update subscription status on subscription table
        // insert user credits with expiration dates
        // update user's total credits
    }

}
