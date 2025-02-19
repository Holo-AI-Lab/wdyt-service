package ai.holo.wdyt.subscription.model.dto;

public record NotificationHistoryRequestDTO(
        Long startDate,
        Long endDate,
        boolean onlyFailures) {
}