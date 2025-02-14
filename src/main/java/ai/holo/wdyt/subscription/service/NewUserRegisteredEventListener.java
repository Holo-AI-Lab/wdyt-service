package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.subscription.repository.UserSubscriptionRepository;
import ai.holo.wdyt.user.model.event.NewUserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@EventConsumer
@Async
@Slf4j
public class NewUserRegisteredEventListener {
    private final UserCreditService userCreditService;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public NewUserRegisteredEventListener(UserCreditService userCreditService, UserSubscriptionRepository userSubscriptionRepository) {
        this.userCreditService = userCreditService;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewUserRegisteredEvent(NewUserRegisteredEvent event) {
        log.info("Handling New User Registered Event: {}", event.getUserId());
        addFreemiumCreditsIfNoSubscription(event.getUserId());
    }

    private void addFreemiumCreditsIfNoSubscription(Long userId) {
        userSubscriptionRepository.findByUserId(userId).ifPresent(userSubscription -> {
            log.info("User already has a subscription, no freemium credits added for user: {}", userId);
            return;
        });
        userCreditService.addFreemiumCredits(userId);
        log.info("Freemium credits added for user: {}", userId);
    }
}
