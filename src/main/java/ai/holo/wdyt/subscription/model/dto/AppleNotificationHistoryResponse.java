package ai.holo.wdyt.subscription.model.dto;

import ai.holo.wdyt.subscription.service.AppleNotificationHistorySyncService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = AppleNotificationHistorySyncService.AppleNotificationHistoryDeserializer.class)
public record AppleNotificationHistoryResponse(
        boolean hasMore,
        String paginationToken,
        List<AppleNotificationItem> notificationHistory
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppleNotificationItem(String signedPayload) {}
}