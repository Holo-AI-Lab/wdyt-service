package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.subscription.model.dto.UserValidCreditsDTO;
import ai.holo.wdyt.subscription.model.entity.SubscriptionPlan;
import ai.holo.wdyt.subscription.model.entity.UserCredit;
import ai.holo.wdyt.subscription.repository.UserCreditRepository;
import ai.holo.wdyt.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserCreditService {
    private final int FREEMIUM_CREDITS = 7;
    private final int FREEMIUM_DURATION_DAYS = 30;
    private static final String CRON_EXPRESSION = "0 0 0 * * ?";


    private final UserCreditRepository creditRepository;

    public UserCreditService(UserCreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    public void addFreemiumCredits(Long userId) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(FREEMIUM_DURATION_DAYS);
        UserCredit newCredit = new UserCredit(userId, FREEMIUM_CREDITS, expirationDate);
        creditRepository.save(newCredit);
    }

    @Scheduled(cron = CRON_EXPRESSION)
    @Transactional
    private void renewFreemiumCredits() {
        // This method is called every day at midnight to renew freemium credits.
        // TODO Implement this method
    }
    public void addCredits(Long userId, Long transactionId, SubscriptionPlan subscriptionPlan) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(subscriptionPlan.getDurationDays());
        UserCredit newCredit = new UserCredit(userId, subscriptionPlan.getCredit(), expirationDate, transactionId);
        creditRepository.save(newCredit);
    }

    public UserValidCreditsDTO getTotalCredits(Long userId) {
        List<UserCredit> validCredits = creditRepository.findValidCreditsByUserId(userId);
        return new UserValidCreditsDTO(validCredits.stream().mapToInt(UserCredit::getCredit).sum());
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
            if (c.getCredit() == 0) {
                c.setValid(false);
            }
            creditRepository.save(c);
        }
    }



    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanExpiredCredits() {
        creditRepository.setInvalidExpiredOrUsedCredits();
    }
}