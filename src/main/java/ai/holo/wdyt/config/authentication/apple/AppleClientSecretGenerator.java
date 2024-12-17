package ai.holo.wdyt.config.authentication.apple;

import io.jsonwebtoken.Jwts;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class AppleClientSecretGenerator {
    private final String teamId;
    private final String keyId;
    private final String privateKey;
    private final String clientId;

    public AppleClientSecretGenerator(String teamId,
                                      String keyId,
                                      String privateKey,
                                      String clientId) {
        this.teamId = teamId;
        this.keyId = keyId;
        this.privateKey = privateKey;
        this.clientId = clientId;
    }

    public String generateClientSecret() {
        try {
            Instant now = Instant.now();

            return Jwts.builder()
                    .header().add(Map.of(
                            "kid", keyId,
                            "alg", "ES256"
                    )).and().claims(Map.of(
                            "iss", teamId,
                            "sub", clientId,
                            "aud", "https://appleid.apple.com",
                            "iat", now.getEpochSecond(),
                            "exp", now.plusSeconds(9000).getEpochSecond()
                    ))
                    .signWith(getPrivateKey())
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating client secret for Apple OAuth", e);
        }
    }

    private PrivateKey getPrivateKey() {
        try {
            String privateKeyContent = privateKey
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "")
                    .trim();
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing private key", e);
        }
    }
}
