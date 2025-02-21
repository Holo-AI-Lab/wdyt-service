package ai.holo.wdyt.subscription.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "user_subscription")
@Getter
@Setter
@NoArgsConstructor
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "subscription_plan")
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscriptionPlan;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;
    @Column(name = "transaction_pending")
    private Boolean transactionPending = false;
    @Column(name = "app_account_token")
    private String appAccountToken;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public UserSubscription(Long userId, String appAccountToken) {
        this.userId = userId;
        this.appAccountToken = appAccountToken;
        this.isActive = false;
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }
}