package ai.holo.wdyt.subscription.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "apple_transaction")
@Getter
@Setter
@NoArgsConstructor
public class AppleTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "subscription_plan")
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscriptionPlan;
    @Column(name = "original_transaction_id", nullable = false)
    private String originalTransactionId;
    @Column(name = "transaction_id", nullable = false)
    private String transactionId;
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public AppleTransaction(Long userId, SubscriptionPlan subscriptionPlan,
                            String originalTransactionId, String transactionId,
                            LocalDateTime purchaseDate) {
        this.userId = userId;
        this.subscriptionPlan = subscriptionPlan;
        this.originalTransactionId = originalTransactionId;
        this.transactionId = transactionId;
        this.purchaseDate = purchaseDate;
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }
}