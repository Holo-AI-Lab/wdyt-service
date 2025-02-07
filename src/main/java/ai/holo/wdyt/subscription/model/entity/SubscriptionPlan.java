package ai.holo.wdyt.subscription.model.entity;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum SubscriptionPlan {
    WEEKLY("1001", 25),
    MONTHLY("1002", 100),
    YEARLY("1003", 1200),
    ONE_TIME_PURCHASE("1004", 10);

    private final String planId;
    private final int credit;

    SubscriptionPlan(String planId, int credit) {
        this.planId = planId;
        this.credit = credit;
    }

    public static Optional<SubscriptionPlan> getPlanByProductId(String planId) {
        for (SubscriptionPlan plan : SubscriptionPlan.values()) {
            if (plan.planId.equals(planId)) {
                return Optional.of(plan);
            }
        }
        return Optional.empty();
    }
}
