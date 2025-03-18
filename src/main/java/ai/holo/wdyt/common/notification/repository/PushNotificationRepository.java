package ai.holo.wdyt.common.notification.repository;

import ai.holo.wdyt.common.notification.model.NotificationType;
import ai.holo.wdyt.common.notification.model.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    boolean existsByUserIdAndNotificationTypeAndCreatedAtAfter(Long id, NotificationType notificationType, LocalDateTime oneWeekAgo);
}
