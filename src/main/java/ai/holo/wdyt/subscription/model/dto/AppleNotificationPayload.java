package ai.holo.wdyt.subscription.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleNotificationPayload(
        String notificationType,
        String subtype,
        String notificationUUID,
        String version,
        AppleNotificationData data) {
}


