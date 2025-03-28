package ai.holo.wdyt.subscription.model.entity;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum SubscriptionPlan {
    WEEKLY("1001", 25, 7, true),
    MONTHLY("1002", 100, 30, true),
    YEARLY("1003", 1200 , 365, true),
    ONE_TIME_PURCHASE("1004", 10 , 7, false);

    private final String planId;
    private final int credit;
    private final int durationDays;
    private final boolean isRecurring;

    SubscriptionPlan(String planId, int credit, int durationDays, boolean isRecurring) {
        this.planId = planId;
        this.credit = credit;
        this.durationDays = durationDays;
        this.isRecurring = isRecurring;
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
