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
    private final int AI_FEEDBACK_COST = 1;

    public AiFeedbackReceivedEventListener(UserCreditService userCreditService, AiFeedbackService aiFeedbackService) {
        this.userCreditService = userCreditService;
        this.aiFeedbackService = aiFeedbackService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAiFeedbackReceivedEvent(AiFeedbackReceivedEvent event) {
        log.info("Handling AiFeedbackReceivedEvent: {}", event.getAiFeedbackId());
        userCreditService.consumeNearestExpiringCredit(event.getUserId(), AI_FEEDBACK_COST);
        log.info("AiFeedback {} consumed {} credit(s)", event.getAiFeedbackId(), AI_FEEDBACK_COST);
    }
}
