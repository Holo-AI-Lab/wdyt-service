package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.subscription.model.entity.UserCredit;
import ai.holo.wdyt.subscription.repository.UserCreditRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCreditService {
    private final UserCreditRepository creditRepository;

    public UserCreditService(UserCreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    public void addCredits(Long userId, int creditAmount, int durationDays) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(durationDays);
        UserCredit newCredit = new UserCredit(userId, creditAmount, expirationDate);
        creditRepository.save(newCredit);
    }

    public void addCredits(Long userId, int creditAmount, int durationDays, Long transactionId) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(durationDays);
        UserCredit newCredit = new UserCredit(userId, creditAmount, expirationDate, transactionId);
        creditRepository.save(newCredit);
    }

    public int getTotalCredits(Long userId) {
        List<UserCredit> validCredits = creditRepository.findValidCreditsByUserId(userId);
        return validCredits.stream().mapToInt(UserCredit::getCredit).sum();
    }
}
