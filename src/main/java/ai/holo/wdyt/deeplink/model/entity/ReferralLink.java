package ai.holo.wdyt.deeplink.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "referral_link")
@Getter
@Setter
@NoArgsConstructor
public class ReferralLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nonce", nullable = false, unique = true)
    private String nonce;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "is_used")
    private boolean isUsed;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "redeemed_at")
    private LocalDateTime redeemedAt;
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    public ReferralLink(Long userId, String nonce) {
        this.userId = userId;
        this.nonce = nonce;
        createdAt = LocalDateTime.now();
        // Referral link expires in one day
        expirationDate = createdAt.plusDays(7);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }
}
