package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.subscription.model.dto.UserDetailedCreditsDTO;
import ai.holo.wdyt.subscription.model.dto.UserValidCreditsDTO;
import ai.holo.wdyt.subscription.model.entity.CreditType;
import ai.holo.wdyt.subscription.model.entity.SubscriptionPlan;
import ai.holo.wdyt.subscription.model.entity.UserCredit;
import ai.holo.wdyt.subscription.repository.AppleTransactionRepository;
import ai.holo.wdyt.subscription.repository.UserCreditRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserCreditService {
    private final int FREEMIUM_CREDITS = 7;
    private final int FREEMIUM_DURATION_DAYS = 30;
    private static final String CRON_EXPRESSION = "0 0 0 * * ?";

    private final UserCreditRepository creditRepository;
    private final AppleTransactionRepository appleTransactionRepository;

    public UserCreditService(UserCreditRepository creditRepository, AppleTransactionRepository appleTransactionRepository) {
        this.creditRepository = creditRepository;
        this.appleTransactionRepository = appleTransactionRepository;
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

    @Transactional
    public UserValidCreditsDTO getTotalCredits(Long userId) {
        List<UserCredit> validCredits = creditRepository.findValidCreditsByUserId(userId);
        return new UserValidCreditsDTO(validCredits.stream().mapToInt(UserCredit::getCredit).sum());
    }

    @Transactional
    public List<UserDetailedCreditsDTO> getDetailedCredits(Long loggedInUserId) {
        List<UserDetailedCreditsDTO> returnObj = new ArrayList<>();
        List<UserCredit> validCredits = creditRepository.findValidCreditsByUserId(loggedInUserId);
        for (UserCredit c : validCredits) {
            long epochSecond;
            if (c.getCreditType().equals(CreditType.FREEMIUM)) {
                epochSecond = c.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
                returnObj.add(new UserDetailedCreditsDTO("FREEMIUM", epochSecond, c.getCredit()));
            } else if (c.getCreditType().equals(CreditType.SUBSCRIPTION)) {
                epochSecond = c.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
                returnObj.add(new UserDetailedCreditsDTO(appleTransactionRepository.
                        findById(c.getTransactionId()).get().getSubscriptionPlan().getPlanId(),
                        epochSecond,
                        c.getCredit()));
            }else{
                log.error("Unknown credit type: {}", c.getCreditType());
            }
        }
        return returnObj;
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