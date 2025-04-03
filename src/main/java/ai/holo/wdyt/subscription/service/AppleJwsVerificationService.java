package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.subscription.model.dto.AppleNotificationPayload;
import ai.holo.wdyt.subscription.model.dto.UserTransactionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AppleJwsVerificationService {
    private final ObjectMapper objectMapper;
    private final Map<String, String> secretProperties;
    private static final String DEFAULT_TEST_KID = "Apple_Xcode_Key";
    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private volatile JWKSet appleJwkSetCache = null;
    private volatile long appleJwkSetCacheTimestamp = 0;
    private static final long CACHE_TTL_MILLIS = 60 * 60 * 10000;

    public AppleJwsVerificationService(ObjectMapper objectMapper, Map<String, String> secretProperties) {
        this.objectMapper = objectMapper;
        this.secretProperties = secretProperties;
    }

    // Verifies and decodes an Apple notification token.
    public AppleNotificationPayload verifyAndDecodeNotification(String jwsToken) {
        try {
            String token = objectMapper.readTree(jwsToken).get("signedPayload").asText();
            String payloadJson = parseJws(token);
            return objectMapper.readValue(payloadJson, AppleNotificationPayload.class);
        } catch (Exception e) {
            log.error("Error verifying Apple JWS: {}", e.getMessage());
            throw new RuntimeException("Error verifying Apple JWS: " + e.getMessage());
        }
    }

    // Verifies and decodes a user transaction token.
    public UserTransactionDto verifyAndDecodeSignedTransaction(String jwsToken) {
        try {
            String payloadJson = parseJws(jwsToken);
            return objectMapper.readValue(payloadJson, UserTransactionDto.class);
        } catch (Exception e) {
            log.error("Error verifying User Transaction JWS: {}", e.getMessage());
            throw new RuntimeException("Error verifying User Transaction JWS: " + e.getMessage());
        }
    }

    private String parseJws(String jwsToken) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(jwsToken);
        JWSHeader header = signedJWT.getHeader();
        String kid = header.getKeyID();

        PublicKey publicKey = getPublicKeyFromX5c(header);
        JWK publicKeyJwk;
        if (publicKey != null) {
            publicKeyJwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) publicKey)
                    .keyID(kid)
                    .build();
        } else {
            if (DEFAULT_TEST_KID.equals(kid)) {
                publicKeyJwk = getLocalJWK();
            } else {
                publicKeyJwk = getApplePublicKey(kid);
            }
        }

        if (publicKeyJwk == null) {
            throw new RuntimeException("No matching public key found for kid: " + kid);
        }

        ECDSAVerifier verifier = new ECDSAVerifier(publicKeyJwk.toECKey());
        if (!signedJWT.verify(verifier)) {
            throw new RuntimeException("Invalid JWS signature.");
        }

        return signedJWT.getPayload().toString();
    }


    // Retrieves and caches the Apple JWKS set.
    private JWKSet getAppleJWKSet() {
        try {
            long now = System.currentTimeMillis();
            if (appleJwkSetCache == null || (now - appleJwkSetCacheTimestamp) > CACHE_TTL_MILLIS) {
                appleJwkSetCache = JWKSet.load(new URL(APPLE_JWKS_URL));
                appleJwkSetCacheTimestamp = now;
                log.debug("Apple JWKS set updated.");
            }
            return appleJwkSetCache;
        } catch (Exception e) {
            log.error("Error fetching Apple JWKS: {}", e.getMessage());
            return null;
        }
    }

    // Gets the Apple public key corresponding to the given kid from the JWKS set.
    private JWK getApplePublicKey(String kid) {
        try {
            JWKSet jwkSet = getAppleJWKSet();
            if (jwkSet == null) {
                throw new RuntimeException("Unable to load Apple JWKS set.");
            }
            return jwkSet.getKeys().stream()
                    .filter(jwk -> kid.equals(jwk.getKeyID()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error fetching Apple public key for kid {}: {}", kid, e.getMessage());
            return null;
        }
    }

    // Builds a local JWK using the embedded public key from secretProperties.
    private JWK getLocalJWK() {
        try {
            String extractedPublicKeyBase64 = secretProperties.get("appleSubscriptionKey");
            if (extractedPublicKeyBase64 == null) {
                throw new RuntimeException("'extractedPublicKey' not found in secret properties.");
            }
            byte[] pubKeyBytes = Base64.getDecoder().decode(extractedPublicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
            String testKid = secretProperties.get("appleSubscriptionKidId");
            return new ECKey.Builder(Curve.P_256, (ECPublicKey) publicKey)
                    .keyID(testKid)
                    .build();
        } catch (Exception e) {
            log.error("Error creating local JWK: {}", e.getMessage());
            return null;
        }
    }

    // Extracts the public key from the x5c field in the JWS header.
    private PublicKey getPublicKeyFromX5c(JWSHeader header) {
        try {
            List<com.nimbusds.jose.util.Base64> x5cList = header.getX509CertChain();
            if (x5cList != null && !x5cList.isEmpty()) {
                String certBase64 = x5cList.get(0).toString();
                byte[] certBytes = Base64.getDecoder().decode(certBase64);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
                return cert.getPublicKey();
            }
        } catch (Exception e) {
            log.error("Error extracting public key from x5c: {}", e.getMessage());
        }
        return null;
    }
}
