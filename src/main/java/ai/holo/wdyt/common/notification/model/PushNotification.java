package ai.holo.wdyt.common.notification.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "user_notification")
@Getter
@Setter
@NoArgsConstructor
public class PushNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType notificationType;
    @Column(name = "name")
    private Long userId;
    @Column(name = "content")
    private String content;
    @Column(name = "is_consumed")
    private boolean isConsumed;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PushNotification(NotificationType notificationType, Long userId, String content) {
        this.notificationType = notificationType;
        this.userId = userId;
        this.content = content;
        this.isConsumed = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
