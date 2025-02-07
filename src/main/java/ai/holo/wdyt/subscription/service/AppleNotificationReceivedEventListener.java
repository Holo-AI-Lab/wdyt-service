package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.subscription.model.entity.AppleNotification;
import ai.holo.wdyt.subscription.model.event.AppleNotificationReceivedEvent;
import ai.holo.wdyt.subscription.repository.AppleNotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@EventConsumer
@Async
@Slf4j
public class AppleNotificationReceivedEventListener {

    private final AppleNotificationRepository appleNotificationRepository;

    public AppleNotificationReceivedEventListener(AppleNotificationRepository appleNotificationRepository) {
        this.appleNotificationRepository = appleNotificationRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionSavedEvent(AppleNotificationReceivedEvent event) {
        // handle event
        log.info("Handling Apple Notification Received Event: {}", event.getAppleNotificationId());

        AppleNotification appleNotification = appleNotificationRepository.findById(event.getAppleNotificationId()).orElseThrow(NotFoundException::new);
        // TODO Transaction information signed by the App Store, in JSON Web Signature (JWS) format.
        String signedTransactionInfo = appleNotification.getSignedTransactionInfo();
        // TODO we need to decode it again and create UserTransactionDto then call createTransaction method on appleSubscriptionService
        // appleSubscriptionService.createTransaction(signedTransactionInfo);
    }

}
