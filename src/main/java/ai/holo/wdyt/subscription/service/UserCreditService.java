package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.subscription.model.dto.UserValidCreditsDTO;
import ai.holo.wdyt.subscription.model.entity.CreditType;
import ai.holo.wdyt.subscription.model.entity.SubscriptionPlan;
import ai.holo.wdyt.subscription.model.entity.UserCredit;
import ai.holo.wdyt.subscription.repository.AppleTransactionRepository;
import ai.holo.wdyt.subscription.repository.UserCreditRepository;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class UserCreditService {
    private final int FREEMIUM_CREDITS = 7;
    private final int FREEMIUM_DURATION_DAYS = 30;
    private static final String CRON_EXPRESSION = "0 0 0 * * ?";

    private final UserCreditRepository creditRepository;
    private final AppleTransactionRepository appleTransactionRepository;
    private final UserService userService;

    public UserCreditService(UserCreditRepository creditRepository, AppleTransactionRepository appleTransactionRepository,
                             UserService userService) {
        this.creditRepository = creditRepository;
        this.appleTransactionRepository = appleTransactionRepository;
        this.userService = userService;
    }

    @Scheduled(cron = CRON_EXPRESSION)
    @Transactional
    public void DailyJobs() {
        creditRepository.setInvalidExpiredOrUsedCredits();
        renewFreemiumCredits();
    }

    @Transactional
    public void addFreemiumCredits(Long userId) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(FREEMIUM_DURATION_DAYS);
        UserCredit newCredit = new UserCredit(userId, FREEMIUM_CREDITS, expirationDate, CreditType.FREEMIUM);
        creditRepository.save(newCredit);
        log.info("Added freemium credits for user {} with expiration {}", userId, expirationDate);
    }

    @Transactional
    public void renewFreemiumCredits() {
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
    public void addCredits(Long userId, Long transactionId, SubscriptionPlan subscriptionPlan, CreditType creditType) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(subscriptionPlan.getDurationDays());
        UserCredit newCredit = new UserCredit(userId, subscriptionPlan.getCredit(), expirationDate, transactionId, creditType);
        creditRepository.save(newCredit);
    }

    @Transactional(readOnly = true)
    public UserValidCreditsDTO getCredit() {
        User user = userService.getUser();

        List<UserCredit> validCredits = creditRepository.findValidCreditsByUserId(user.getId());
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
        List<UserCredit> credits = creditRepository.findValidCreditsByUserId(userId);
        int remainingToConsume = credit;
        for (UserCredit c : credits) {
            if (remainingToConsume <= 0) {
                break;
            }
            int available = c.getCredit();
            if (available >= remainingToConsume) {
                c.setCredit(available - remainingToConsume);
                remainingToConsume = 0;
            } else {
                c.setCredit(0);
                remainingToConsume -= available;
            }
            creditRepository.save(c);
        }
    }
}