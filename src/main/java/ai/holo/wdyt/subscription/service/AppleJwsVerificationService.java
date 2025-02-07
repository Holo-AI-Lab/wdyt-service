package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.subscription.model.dto.AppleNotificationPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.jwk.JWK;

import org.springframework.stereotype.Service;

@Service
public class AppleJwsVerificationService {
    private final ObjectMapper objectMapper;

    public AppleJwsVerificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AppleNotificationPayload verifyAndDecode(String jwsToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwsToken);
            JWSHeader header = signedJWT.getHeader();
            String kid = header.getKeyID();

            //    Find Apple Public Key.
            //    In production, we need to get it from the Apple JWKS endpoint and cache it. (Like apple login mechanism)
            JWK applePublicKey = getApplePublicKey(kid);
            if (applePublicKey == null) {
                throw new RuntimeException("No matching public key found for kid: " + kid);
            }

            ECDSAVerifier verifier = new ECDSAVerifier(applePublicKey.toECKey());
            boolean isValid = signedJWT.verify(verifier);
            if (!isValid) {
                throw new RuntimeException("Invalid JWS signature");
            }

            String payloadJson = signedJWT.getPayload().toString();

            AppleNotificationPayload payload =
                    objectMapper.readValue(payloadJson, AppleNotificationPayload.class);
            return payload;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     *  - Return JWK object .
     */
    private JWK getApplePublicKey(String kid) {
        // TODO Public key logic.
        return null;
    }
}
