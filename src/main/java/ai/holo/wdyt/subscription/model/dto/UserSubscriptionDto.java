package ai.holo.wdyt.subscription.model.dto;

import ai.holo.wdyt.subscription.model.entity.UserSubscription;

public record UserSubscriptionDto(Long userId, String appAccountToken) {
    public UserSubscriptionDto(UserSubscription subscription) {
        this(subscription.getUserId(), subscription.getAppAccountToken());
    }
}
