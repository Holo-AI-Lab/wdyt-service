package ai.holo.wdyt.subscription.model.dto;

import ai.holo.wdyt.subscription.model.entity.SubscriptionPlan;

import java.time.ZonedDateTime;

public record UserValidCreditsDTO(int totalCredit, SubscriptionPlan activePlan, ZonedDateTime expirationDate) {
}
