package ai.holo.wdyt.subscription.model.entity;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum SubscriptionPlan {
    WEEKLY("1001", 25, 7),
    MONTHLY("1002", 100, 30),
    YEARLY("1003", 1200 , 365),
    ONE_TIME_PURCHASE("1004", 10 , 30);

    private final String planId;
    private final int credit;
    private final int durationDays;

    SubscriptionPlan(String planId, int credit, int durationDays) {
        this.planId = planId;
        this.credit = credit;
        this.durationDays = durationDays;
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
