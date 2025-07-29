package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.event.AiFeedbackReceivedEvent;
import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.subscription.service.UserCreditService;
import ai.holo.wdyt.user.repository.UserRepository;
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
    UserRepository userRepository;
    public AiFeedbackReceivedEventListener(UserCreditService userCreditService, AiFeedbackService aiFeedbackService, UserRepository userRepository) {
        this.userCreditService = userCreditService;
        this.aiFeedbackService = aiFeedbackService;
        this.userRepository = userRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAiFeedbackReceivedEvent(AiFeedbackReceivedEvent event) {
        log.info("Handling AiFeedbackReceivedEvent: {}", event.getAiFeedbackId());
        userCreditService.consumeFromNearestExpiringCredit(event.getFeedbackReceiverUserId(), UserCreditService.AI_FEEDBACK_COST);
        log.info("AiFeedback {} consumed {} credit(s)", event.getAiFeedbackId(), UserCreditService.AI_FEEDBACK_COST);
    }
}
