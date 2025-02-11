package ai.holo.wdyt.deeplink.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "client_fingerprint")
@Getter
@Setter
@NoArgsConstructor
public class ClientFingerprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nonce")
    private String nonce;
    @Column(name = "user_fingerprint")
    private String userFingerprint;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;


    public ClientFingerprint(String nonce, String userFingerprint) {
        this.nonce = nonce;
        this.userFingerprint = userFingerprint;
        createdAt = LocalDateTime.now();
        // Client fingerprint expires in one day
        expirationDate = createdAt.plusDays(1);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }
}
