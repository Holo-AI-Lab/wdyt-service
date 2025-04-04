package ai.holo.wdyt.subscription.repository;

import ai.holo.wdyt.subscription.model.entity.AppleTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppleTransactionRepository extends JpaRepository<AppleTransaction, Long> {

    void deleteAllByUserId(Long id);
    boolean existsByTransactionId(String transactionId);
}
