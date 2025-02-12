package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.dto.AiFeedbackDetailedDto;
import ai.holo.wdyt.askai.model.event.AiFeedbackReceivedEvent;
import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.subscription.service.UserCreditService;
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
UserCreditService userCreditService;
AiFeedbackService aiFeedbackService;

    public AiFeedbackReceivedEventListener(UserCreditService userCreditService, AiFeedbackService aiFeedbackService) {
        this.userCreditService = userCreditService;
        this.aiFeedbackService = aiFeedbackService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAiFeedbackReceivedEvent(AiFeedbackReceivedEvent event) {
        // handle event
        log.info("Handling AiFeedbackReceivedEvent: {}", event.getAiFeedbackId());
        AiFeedbackDetailedDto aiFeedback = aiFeedbackService.getAiFeedback(event.getAiFeedbackId());
        Long userId = aiFeedback.userInfo().id();
        consumeCredit(userId);

    }

    private void consumeCredit(Long userId) {
        userCreditService.consumeNearestExpiringCredit(userId);
        log.info("Credit consumed from user: {}", userId);
    }

}
