package ai.holo.wdyt.subscription.model.dto;

import ai.holo.wdyt.subscription.model.entity.UserSubscription;

public record UserSubscriptionDto(Long userId, String appAccountToken, String subscriptionPlanName, String subscriptionPlanId,
                                  Boolean isActive, Boolean transactionPending) {
    public UserSubscriptionDto(UserSubscription subscription) {
        this(subscription.getUserId(),
                subscription.getAppAccountToken(),
                (subscription.getSubscriptionPlan() == null) ? null : subscription.getSubscriptionPlan().name(),
                (subscription.getSubscriptionPlan() == null) ? null : subscription.getSubscriptionPlan().getPlanId(),
                subscription.getIsActive() ,
                subscription.getTransactionPending());
    }
}