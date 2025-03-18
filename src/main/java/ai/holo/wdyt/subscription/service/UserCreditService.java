package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.subscription.model.dto.UserValidCreditsDTO;
import ai.holo.wdyt.subscription.model.entity.CreditType;
import ai.holo.wdyt.subscription.model.entity.SubscriptionPlan;
import ai.holo.wdyt.subscription.model.entity.UserCredit;
import ai.holo.wdyt.subscription.repository.AppleTransactionRepository;
import ai.holo.wdyt.subscription.repository.UserCreditRepository;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.repository.UserRepository;
import ai.holo.wdyt.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class UserCreditService {
    private final int FREEMIUM_CREDITS = 7;
    private final int FREEMIUM_DURATION_DAYS = 30;
    public static final int AI_FEEDBACK_COST = 1;

    private final UserCreditRepository creditRepository;
    private final AppleTransactionRepository appleTransactionRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public UserCreditService(UserCreditRepository creditRepository, AppleTransactionRepository appleTransactionRepository,
                             UserService userService, UserRepository userRepository) {
        this.creditRepository = creditRepository;
        this.appleTransactionRepository = appleTransactionRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public void addFreemiumCredits(Long userId) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(FREEMIUM_DURATION_DAYS);
        UserCredit newCredit = new UserCredit(userId, FREEMIUM_CREDITS, expirationDate, CreditType.FREEMIUM);
        increaseUserCredit(userId, newCredit);
        log.info("Added freemium credits for user {} with expiration {}", userId, expirationDate);
    }

    protected void renewFreemiumCredits() {
        List<UserCredit> expiredFreemiumCredits = creditRepository.findExpiredFreemiumCredits(CreditType.FREEMIUM);
        LocalDateTime now = LocalDateTime.now();
        log.info("Processing {} expired freemium credit records at {}", expiredFreemiumCredits.size(), now);
        for (UserCredit expiredCredit : expiredFreemiumCredits) {
            Long userId = expiredCredit.getUserId();
            boolean hasActiveSubscription = creditRepository.existsByUserIdAndCreditTypeAndExpiresAtGreaterThan(userId, CreditType.SUBSCRIPTION, now);
            if (!hasActiveSubscription) {
                addFreemiumCredits(userId);
                log.info("User {} does not have an active subscription. Freemium credits renewed.", userId);
            } else {
                log.info("User {} has an active subscription. No freemium renewal performed.", userId);
            }
        }
    }

    @Transactional
    public void markExpiredCreditsAsInvalid() {
        creditRepository.findExpiredCredits().forEach(userCredit -> {
            userCredit.setValid(false);
            creditRepository.save(userCredit);
            User user = userService.getUserById(userCredit.getUserId());
            user.decreaseCreditBalance(userCredit.getCredit());
            userRepository.save(user);
        });
    }

    @Transactional
    public void markConsumedCreditsToInvalid() {
        creditRepository.findConsumedCredits().forEach(userCredit -> {
            userCredit.setValid(false);
            creditRepository.save(userCredit);
        });
    }

    @Transactional
    public void addCredits(Long userId, Long transactionId, SubscriptionPlan subscriptionPlan, CreditType creditType) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(subscriptionPlan.getDurationDays());
        UserCredit newCredit = new UserCredit(userId, subscriptionPlan.getCredit(), expirationDate, transactionId, creditType);
        increaseUserCredit(userId, newCredit);
    }

    @Transactional(readOnly = true)
    public UserValidCreditsDTO getCredit() {
        User user = userService.getUser();

        List<UserCredit> validCredits = creditRepository.findValidCreditsByUserIdSortedByExpiresAt(user.getId());
        UserCredit activeSubscriptionCredit = validCredits.stream()
                .filter(c -> CreditType.SUBSCRIPTION.equals(c.getCreditType()))
                .max(Comparator.comparing(UserCredit::getExpiresAt))
                .orElse(null);
        SubscriptionPlan activeSubscriptionPlan = activeSubscriptionCredit != null ?
                appleTransactionRepository.findById(activeSubscriptionCredit.getTransactionId()).get().getSubscriptionPlan() : null;
        ZonedDateTime expiredDate = activeSubscriptionCredit != null ? activeSubscriptionCredit.getExpiresAt().atZone(ZoneId.systemDefault()) : null;

        return new UserValidCreditsDTO(validCredits.stream().mapToInt(UserCredit::getCredit).sum(),
                activeSubscriptionPlan, expiredDate);
    }

    @Transactional
    public void consumeNearestExpiringCredit(Long userId, int credit) {
        List<UserCredit> credits = creditRepository.findValidCreditsByUserIdSortedByExpiresAt(userId);
        AtomicInteger remainingToConsume = new AtomicInteger(credit);

        credits.forEach(c -> {
            if (remainingToConsume.get() > 0) {
                int available = c.getCredit();
                int consumeAmount = Math.min(available, remainingToConsume.get());
                c.decreaseCredit(consumeAmount);
                remainingToConsume.addAndGet(-consumeAmount);
            }
        });
        creditRepository.saveAll(credits);
        decreaseUserCredit(userId, credit);
    }

    private void increaseUserCredit(Long userId ,UserCredit newCredit) {
        creditRepository.save(newCredit);
        User user = userService.getUserById(userId);
        user.increaseCreditBalance(newCredit.getCredit());
        userRepository.save(user);
    }

    private void decreaseUserCredit(Long userId, int creditToSubtract) {
        User user = userService.getUserById(userId);
        user.decreaseCreditBalance(creditToSubtract);
        userRepository.save(user);
    }
}