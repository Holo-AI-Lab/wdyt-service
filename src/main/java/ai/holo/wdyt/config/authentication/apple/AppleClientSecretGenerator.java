package ai.holo.wdyt.config.authentication.apple;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Jwts;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
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
    private final String bundleId;

    public AppleClientSecretGenerator(String teamId,
                                      String keyId,
                                      String privateKey,
                                      String clientId,
                                      String bundleId) {
        this.teamId = teamId;
        this.keyId = keyId;
        this.privateKey = privateKey;
        this.clientId = clientId;
        this.bundleId = bundleId;
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

    public String generateAppleJwtFromKeyString() throws Exception {
        ECPrivateKey ecPrivateKey = (ECPrivateKey) getPrivateKey();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).type(JOSEObjectType.JWT).build();

        long nowInSeconds = System.currentTimeMillis() / 1000;
        Date issuedAt = new Date(nowInSeconds * 1000);
        Date expiration = new Date((nowInSeconds + (20 * 60)) * 1000); // 20 minutes later

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(teamId)
                .issueTime(issuedAt)
                .expirationTime(expiration)
                .audience("appstoreconnect-v1")
                .claim("bid", bundleId)
                .build();
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        JWSSigner signer = new ECDSASigner(ecPrivateKey);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
}
