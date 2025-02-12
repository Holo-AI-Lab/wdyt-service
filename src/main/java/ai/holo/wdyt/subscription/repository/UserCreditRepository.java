package ai.holo.wdyt.subscription.repository;

import ai.holo.wdyt.subscription.model.entity.UserCredit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {

    @Query("SELECT c FROM user_credit c WHERE c.userId = :userId AND c.valid= true AND c.expiresAt > CURRENT_TIMESTAMP AND c.credit > 0")
    List<UserCredit> findValidCreditsByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = """
       UPDATE user_credit
       SET credit = credit - 1
       WHERE id = (
           SELECT id FROM user_credit
           WHERE user_id = :userId
           AND expires_at > CURRENT_TIMESTAMP
           AND credit > 0
           ORDER BY expires_at ASC
           LIMIT 1 )
    """, nativeQuery = true)
    void consumeNearestExpiringCredit(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserCredit c SET c.valid = false WHERE c.expires_at < CURRENT_TIMESTAMP OR c.credit <= 0")
    void setInvalidExpiredOrUsedCredits();

}