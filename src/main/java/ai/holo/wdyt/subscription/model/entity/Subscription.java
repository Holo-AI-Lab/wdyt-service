package ai.holo.wdyt.subscription.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "original_transaction_id", nullable = false)
    private String originalTransactionId;

    @Column(name = "last_transaction_id")
    private String lastTransactionId;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "expire_date")
    private Instant expireDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "credits", nullable = false)
    private Integer credits = 0;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;
}