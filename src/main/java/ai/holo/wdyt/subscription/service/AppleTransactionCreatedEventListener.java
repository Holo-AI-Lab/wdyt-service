package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.subscription.model.entity.AppleTransaction;
import ai.holo.wdyt.subscription.model.entity.CreditType;
import ai.holo.wdyt.subscription.model.entity.SubscriptionPlan;
import ai.holo.wdyt.subscription.model.entity.UserSubscription;
import ai.holo.wdyt.subscription.model.event.AppleTransactionCreatedEvent;
import ai.holo.wdyt.subscription.repository.AppleTransactionRepository;
import ai.holo.wdyt.subscription.repository.UserCreditRepository;
import ai.holo.wdyt.subscription.repository.UserSubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@EventConsumer
@Async
@Slf4j
public class AppleTransactionCreatedEventListener {

    private final AppleTransactionRepository appleTransactionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserCreditService userCreditService;
    private final UserCreditRepository userCreditRepository;

    public AppleTransactionCreatedEventListener(AppleTransactionRepository appleTransactionRepository, UserSubscriptionRepository userSubscriptionRepository, UserCreditService userCreditService, UserCreditRepository userCreditRepository) {
        this.appleTransactionRepository = appleTransactionRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userCreditService = userCreditService;
        this.userCreditRepository = userCreditRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionSavedEvent(AppleTransactionCreatedEvent event) {
        log.info("Handling Apple Transaction Created Event: {}", event.getAppleTransactionId());
        Optional<AppleTransaction> appleTransaction = appleTransactionRepository.findById(event.getAppleTransactionId());
        if (appleTransaction.isEmpty()) {
            log.error("Apple transaction not found for id: {}", event.getAppleTransactionId());
            throw new RuntimeException("Apple transaction not found for id: " + event.getAppleTransactionId());
        }else {
            updateSubscriptionStatus(appleTransaction.get());
            addCredits(appleTransaction.get());
        }
    }

    private void updateSubscriptionStatus(AppleTransaction appleTransaction) {
        Optional<UserSubscription> userSubscription = userSubscriptionRepository.findByUserId(appleTransaction.getUserId());
        if (userSubscription.isEmpty()) {
            log.error("User subscription not found for userId: {}", appleTransaction.getUserId());
            throw new RuntimeException("User subscription not found for userId: " + appleTransaction.getUserId());
        }
        if (!appleTransaction.getSubscriptionPlan().isRecurring()) {
            log.info("A non-recurring transaction has been made, no need to update subscription status for userId: {}", appleTransaction.getUserId());
            return;
        }
        updateCreditsOnSubscriptionUpgrade(appleTransaction);

        UserSubscription userSubscriptionSave = userSubscription.get();
        userSubscriptionSave.setIsActive(true);
        userSubscriptionSave.setTransactionPending(false);
        userSubscriptionSave.setLastUpdatedAt(LocalDateTime.now());
        userSubscriptionSave.setSubscriptionPlan(appleTransaction.getSubscriptionPlan());
        userSubscriptionRepository.save(userSubscriptionSave);
        log.info("User subscription updated for userId: {}", appleTransaction.getUserId());
    }

    private void updateCreditsOnSubscriptionUpgrade(AppleTransaction appleTransaction) {
        UserSubscription userSubscription = userSubscriptionRepository
                .findByUserId(appleTransaction.getUserId()).orElseThrow(NotFoundException::new);
            boolean isRecurring = appleTransaction.getSubscriptionPlan().isRecurring();
            
            if ((userSubscription.getSubscriptionPlan() == null) && isRecurring) {
                refreshCreditExpireDates(appleTransaction);
            } else if (isRecurring && userSubscription.getSubscriptionPlan().isRecurring()) {
                if (userSubscription.getSubscriptionPlan().getCredit() < appleTransaction.getSubscriptionPlan().getCredit()) {
                    refreshCreditExpireDates(appleTransaction);
                }
            }
    }

    private void refreshCreditExpireDates(AppleTransaction appleTransaction) {
        log.info("User subscription plan has been upgraded, updating credits for userId: {}", appleTransaction.getUserId());
        userCreditRepository.findValidCreditsByUserIdSortedByExpiresAt(appleTransaction.getUserId())
                .forEach(credit -> {
                    LocalDateTime newExpireDate = LocalDateTime.now().plusDays(appleTransaction.getSubscriptionPlan().getDurationDays());
                    credit.setExpiresAt(newExpireDate);
                    userCreditRepository.save(credit);
                    log.info("Updated credit expiration date for userId: {} to {}", appleTransaction.getUserId(), newExpireDate);
                });
    }

    private void addCredits (AppleTransaction appleTransaction){
            Long userId = appleTransaction.getUserId();
            SubscriptionPlan subscriptionPlan = appleTransaction.getSubscriptionPlan();
            CreditType creditType = SubscriptionPlan.ONE_TIME_PURCHASE.equals(subscriptionPlan) ? CreditType.ONE_TIME_PURCHASE : CreditType.RECURRING_PURCHASE;
            userCreditService.addCredits(userId, appleTransaction.getId(), subscriptionPlan, creditType);
            log.info("Credits added for transactionId: {}", appleTransaction.getId());
        }
    }
