package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.subscription.model.dto.UserTransactionDto;
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
    private final AppleJwsVerificationService appleJwsVerificationService;
    private final AppleSubscriptionService appleSubscriptionService;

    public AppleNotificationReceivedEventListener(AppleNotificationRepository appleNotificationRepository, AppleJwsVerificationService appleJwsVerificationService, AppleSubscriptionService appleSubscriptionService) {
        this.appleNotificationRepository = appleNotificationRepository;
        this.appleJwsVerificationService = appleJwsVerificationService;
        this.appleSubscriptionService = appleSubscriptionService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionSavedEvent(AppleNotificationReceivedEvent event) {
        // handle event
        log.info("Handling Apple Notification Received Event: {}", event.getAppleNotificationId());

        AppleNotification appleNotification = appleNotificationRepository.findById(event.getAppleNotificationId()).orElseThrow(NotFoundException::new);
        String notificationType = appleNotification.getNotificationType();
        if (notificationType.equals("SUBSCRIBED") || notificationType.equals("DID_RENEW") || notificationType.equals("ONE_TIME_CHARGE")) {
            String signedTransactionInfo = appleNotification.getSignedTransactionInfo();
            UserTransactionDto userTransactionDto = appleJwsVerificationService.verifyAndDecodeSignedTransaction(signedTransactionInfo);
            appleSubscriptionService.createTransaction(userTransactionDto, false);
            log.info("Received notification with Id {} and type {} processed successfully, transaction created.", appleNotification.getId(), notificationType);
            log.warn("Received notification with Id {} and type {}. Signed Transaction info: {}", appleNotification.getId(), notificationType, userTransactionDto);
        }

        updateSubscriptionPendingStatus(appleNotification);
    }

    private void updateSubscriptionPendingStatus(AppleNotification appleNotification) {
        String signedTransactionInfo = appleNotification.getSignedTransactionInfo();
        if (signedTransactionInfo != null) {
            UserTransactionDto userTransactionDto = appleJwsVerificationService.verifyAndDecodeSignedTransaction(signedTransactionInfo);
            appleSubscriptionService.updateTransactionPending(userTransactionDto.appAccountToken(), false);
        }
    }
}
