package ai.holo.wdyt.subscription.repository;

import ai.holo.wdyt.subscription.model.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByOriginalTransactionId(String originalTransactionId);
}
