package ai.holo.wdyt.subscription.repository;

import ai.holo.wdyt.subscription.model.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserId(Long userId);
    Optional<UserSubscription> findByAppAccountToken(String appAccountToken);
}
