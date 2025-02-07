package ai.holo.wdyt.subscription.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleNotificationPayload(
        @JsonProperty("notificationType") String notificationType,
        @JsonProperty("subtype") String subtype,
        @JsonProperty("notificationUUID") String notificationUUID,
        @JsonProperty("version") String version,
        @JsonProperty("data") AppleNotificationData data) {
}


