package ai.holo.wdyt.common.notification.builder;

import ai.holo.wdyt.common.notification.model.NotificationType;
import ai.holo.wdyt.common.notification.service.PushNotificationService;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PushNotificationBuilder {
    private final NotificationType type;
    private final Long userId;
    private final String title;
    private final String message;

    @Setter
    protected static PushNotificationService pushNotificationService;

    private PushNotificationBuilder(Builder builder) {
        this.type = builder.type;
        this.userId = builder.userId;
        this.title = builder.title;
        this.message = builder.message;
    }

    public static Builder builder(NotificationType type) {
        return new Builder(type);
    }

    public static class Builder {
        private final NotificationType type;
        private Long userId;
        private String title;
        private String message;

        public Builder(NotificationType type) {
            if (type == null) throw new IllegalArgumentException("Notification type must not be null.");
            this.type = type;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public PushNotificationBuilder build() {
            if (userId == null) throw new IllegalStateException("User ID must be provided.");
            if (title == null) throw new IllegalStateException("Title must be provided.");
            if (message == null) throw new IllegalStateException("Message must be provided.");
            return new PushNotificationBuilder(this);
        }

        public void sendNotification() {
            PushNotificationBuilder notificationBuilder = build();
            if (pushNotificationService == null) throw new IllegalStateException("PushNotificationService is not configured.");
            pushNotificationService.sendPushNotification(
                    notificationBuilder.getUserId(),
                    notificationBuilder.getTitle(),
                    notificationBuilder.getMessage(),
                    notificationBuilder.getType()
            );
        }
    }
}