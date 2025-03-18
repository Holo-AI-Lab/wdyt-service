package ai.holo.wdyt.subscription.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "user_credit")
@Getter
@Setter
@NoArgsConstructor
public class UserCredit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "credit")
    private int credit;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "valid")
    private boolean valid;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_type", nullable = false)
    private CreditType creditType;

    public UserCredit(Long userId, int credit, LocalDateTime expiresAt, CreditType creditType) {
        this.userId = userId;
        this.credit = credit;
        this.expiresAt = expiresAt;
        this.creditType = creditType;
        this.valid = true;
    }

    public UserCredit(Long userId, int credit, LocalDateTime expiresAt, Long transactionId, CreditType creditType) {
        this.userId = userId;
        this.credit = credit;
        this.expiresAt = expiresAt;
        this.valid = true;
        this.creditType = creditType;
        this.transactionId = transactionId;
    }

    public void decreaseCredit(int amount) {
        this.credit -= amount;
    }
}