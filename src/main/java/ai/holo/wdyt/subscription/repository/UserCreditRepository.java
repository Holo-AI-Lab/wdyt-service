package ai.holo.wdyt.subscription.repository;

import ai.holo.wdyt.subscription.model.entity.CreditType;
import ai.holo.wdyt.subscription.model.entity.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {

    @Query("SELECT c FROM user_credit c WHERE c.userId = :userId AND c.valid= true AND c.expiresAt > CURRENT_TIMESTAMP AND c.credit > 0 ORDER BY c.expiresAt ASC, c.credit ASC")
    List<UserCredit> findValidCreditsByUserIdSortedByExpiresAt(@Param("userId") Long userId);

    @Query("SELECT c FROM user_credit c WHERE c.valid = true AND c.credit <= 0")
    List<UserCredit> findConsumedCredits();

    @Query("SELECT c FROM user_credit c WHERE c.valid = true AND c.expiresAt < CURRENT_TIMESTAMP")
    List<UserCredit> findExpiredCredits();

    boolean existsByUserIdAndCreditTypeAndExpiresAtGreaterThan(Long userId, CreditType creditType, LocalDateTime now);
    void deleteAllByUserId(Long id);
}