package ai.holo.wdyt.common.notification.repository;

import ai.holo.wdyt.common.notification.model.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
}
