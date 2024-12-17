package ai.holo.wdyt.auth.service;

import ai.holo.wdyt.user.model.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final String secretKey;

    public JwtService(Map<String, String> secretProperties) {
        this.secretKey = secretProperties.get("apiJwtSecret");
    }

    public String generateJwtToken(User user) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(90 * 24 * 60 * 60); // 90 days in seconds

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .signWith(getSecretKey())
                .compact();
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
