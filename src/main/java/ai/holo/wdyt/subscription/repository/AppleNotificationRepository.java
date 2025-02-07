package ai.holo.wdyt.subscription.repository;

import ai.holo.wdyt.subscription.model.entity.AppleNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppleNotificationRepository extends JpaRepository<AppleNotification, Long> {
    boolean existByNotificationId(String transactionId);
}
