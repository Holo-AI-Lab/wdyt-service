package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.event.AiFeedbackReceivedEvent;
import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.subscription.service.UserCreditService;
import ai.holo.wdyt.user.model.entity.User;
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
        userCreditService.consumeNearestExpiringCredit(event.getFeedbackReceiverUserId(), UserCreditService.AI_FEEDBACK_COST);
        log.info("AiFeedback {} consumed {} credit(s)", event.getAiFeedbackId(), UserCreditService.AI_FEEDBACK_COST);
        increaseReceivedFeedbackCountForTheUser(event.getFeedbackReceiverUserId());
        increaseGivenFeedbackCountForTheUser(event.getFeedbackGiverUserId());
    }

    private void increaseReceivedFeedbackCountForTheUser(Long receiverFeedbackUserId) {
        User user = userRepository.findById(receiverFeedbackUserId).orElseThrow(NotFoundException::new);
        user.increaseReceivedFeedbacks();
        userRepository.save(user);
    }

    private void increaseGivenFeedbackCountForTheUser(Long giverFeedbackUserId) {
        User user = userRepository.findById(giverFeedbackUserId).orElseThrow(NotFoundException::new);
        user.increaseGivenFeedbacks();
        userRepository.save(user);
    }
}
