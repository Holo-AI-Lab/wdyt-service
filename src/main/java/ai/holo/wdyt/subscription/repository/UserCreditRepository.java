package ai.holo.wdyt.subscription.repository;

import ai.holo.wdyt.subscription.model.entity.CreditType;
import ai.holo.wdyt.subscription.model.entity.UserCredit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {

    @Query("SELECT c FROM user_credit c WHERE c.userId = :userId AND c.valid= true AND c.expiresAt > CURRENT_TIMESTAMP AND c.credit > 0 ORDER BY c.expiresAt ASC")
    List<UserCredit> findValidCreditsByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE user_credit c SET c.valid = false WHERE c.valid = true and (c.expiresAt < CURRENT_TIMESTAMP OR c.credit <= 0)")
    void setInvalidExpiredOrUsedCredits();

    @Query("SELECT c FROM user_credit c WHERE c.creditType = :creditType AND c.expiresAt <= CURRENT_TIMESTAMP")
    List<UserCredit> findExpiredFreemiumCredits(@Param("creditType") CreditType creditType);

    boolean existsByUserIdAndCreditTypeAndExpiresAtGreaterThan(Long userId, CreditType creditType, LocalDateTime now);
}