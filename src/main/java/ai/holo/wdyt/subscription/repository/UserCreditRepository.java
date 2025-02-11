package ai.holo.wdyt.subscription.repository;

import ai.holo.wdyt.subscription.model.entity.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {

    @Query("SELECT c FROM user_credit c WHERE c.userId = :userId AND c.valid= true AND c.expiresAt > CURRENT_TIMESTAMP AND c.credit > 0")
    List<UserCredit> findValidCreditsByUserId(@Param("userId") Long userId);
}