package ai.holo.wdyt.user.service;

import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.subscription.model.entity.UserSubscription;
import ai.holo.wdyt.subscription.repository.UserSubscriptionRepository;
import ai.holo.wdyt.subscription.service.UserCreditService;
import ai.holo.wdyt.user.model.event.NewUserRegisteredEvent;
import ai.holo.wdyt.wardrobe.repository.WardrobeRepository;
import ai.holo.wdyt.wardrobe.service.WardrobeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Component
@EventConsumer
@Async
@Slf4j
public class NewUserRegisteredEventListener {
    private final UserCreditService userCreditService;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final WardrobeRepository wardrobeRepository;
    private final WardrobeService wardrobeService;

    public NewUserRegisteredEventListener(UserCreditService userCreditService, UserSubscriptionRepository userSubscriptionRepository, WardrobeRepository wardrobeRepository, WardrobeService wardrobeService) {
        this.userCreditService = userCreditService;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.wardrobeRepository = wardrobeRepository;
        this.wardrobeService = wardrobeService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewUserRegisteredEvent(NewUserRegisteredEvent event) {
        log.info("Handling New User Registered Event: {}", event.getUserId());
        addFreemiumCreditsIfNoSubscriptionExisting(event.getUserId());
        createWardrobeForUser(event.getUserId());
    }

    private void createWardrobeForUser(Long userId) {
        if (!wardrobeRepository.existsByUserId(userId)) {
            wardrobeService.createWardrobeForUser(userId);
            log.info("Wardrobe created for user: {}", userId);
        } else {
            log.info("Wardrobe already exists for user: {}", userId);
        }
    }

    private void addFreemiumCreditsIfNoSubscriptionExisting(Long userId) {
        Optional<UserSubscription> userSubscription = userSubscriptionRepository.findByUserId(userId);
        if (userSubscription.isPresent()) {
            log.info("User already has a subscription, no freemium credits added for user: {}", userId);
        } else {
            userCreditService.addFreemiumCredits(userId);
            log.info("Freemium credits added for user: {}", userId);
        }
    }
}
