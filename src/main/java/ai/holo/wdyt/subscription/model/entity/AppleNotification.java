package ai.holo.wdyt.subscription.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "apple_notification")
@Getter
@Setter
@NoArgsConstructor
public class AppleNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "notification_id", nullable = false)
    private String notificationId;
    @Column(name = "notification_type")
    private String notificationType;
    @Column(name = "subtype")
    private String subType;
    @Column(name = "notification_version")
    private String appleNotificationVersion;
    @Column(name = "signed_transaction_info")
    private String signedTransactionInfo;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AppleNotification(String notificationId,
                             String notificationType,
                             String subType,
                             String appleNotificationVersion,
                             String getSignedTransactionInfo) {
        this.notificationId = notificationId;
        this.notificationType = notificationType;
        this.subType = subType;
        this.appleNotificationVersion = appleNotificationVersion;
        this.signedTransactionInfo = getSignedTransactionInfo;
        this.createdAt = LocalDateTime.now();
    }
}