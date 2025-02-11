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

    public UserCredit(Long userId, int credit, LocalDateTime expiresAt) {
        this.userId = userId;
        this.credit = credit;
        this.expiresAt = expiresAt;
        this.valid = true;
    }

    public UserCredit(Long userId, int credit, LocalDateTime expiresAt, Long transactionId) {
        this.userId = userId;
        this.credit = credit;
        this.expiresAt = expiresAt;
        this.valid = true;
        this.transactionId = transactionId;
    }
}