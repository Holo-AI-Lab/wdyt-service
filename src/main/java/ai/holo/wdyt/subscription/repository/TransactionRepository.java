package ai.holo.wdyt.subscription.repository;

import ai.holo.wdyt.subscription.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
