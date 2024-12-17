package ai.holo.wdyt.auth.service;

import ai.holo.wdyt.config.authentication.apple.AppleClientSecretGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AppleSignInService {
    private final AppleClientSecretGenerator clientSecretGenerator;
    private final ApplePublicKeyCache keyCache;
    private final String clientId;

    public AppleSignInService(AppleClientSecretGenerator clientSecretGenerator,
                              ApplePublicKeyCache keyCache,
                              Map<String, String> secretProperties) {
        this.clientSecretGenerator = clientSecretGenerator;
        this.keyCache = keyCache;
        this.clientId = secretProperties.get("appleClientId");
    }

    public Claims authenticateWithApple(String authorizationCode) {
        String idToken = retrieveIdToken(authorizationCode);
        return validateIdToken(idToken);
    }

    private String retrieveIdToken(String authorizationCode) {
        String clientSecret = clientSecretGenerator.generateClientSecret();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", authorizationCode);
        formData.add("grant_type", "authorization_code");

        WebClient webClient = WebClient.builder()
                .baseUrl("https://appleid.apple.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        var tokenResponse = webClient.post()
                .uri("/auth/token")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (tokenResponse == null || tokenResponse.get("id_token") == null) {
            throw new IllegalArgumentException("Invalid response from Apple");
        }

        return tokenResponse.get("id_token").toString();
    }

    private Claims validateIdToken(String idToken) {
        try {
            // Fetch cached public keys from Apple
            List<ApplePublicKeyCache.ApplePublicKey> applePublicKeys = keyCache.getCachedAppleKeys();

            // Parse and validate the JWT using the custom key locator
            Jws<Claims> claimsJws = Jwts.parser()
                    .keyLocator(jwsHeader -> {
                        // Get the 'kid' from the JWT header
                        String kid = (String) jwsHeader.get("kid");

                        // Find the corresponding public key based on 'kid'
                        Optional<ApplePublicKeyCache.ApplePublicKey> keyOpt = applePublicKeys.stream()
                                .filter(k -> k.kid().equals(kid))
                                .findFirst();

                        if (keyOpt.isPresent()) {
                            ApplePublicKeyCache.ApplePublicKey key = keyOpt.get();
                            byte[] modulus = Base64.getUrlDecoder().decode(key.n());
                            byte[] exponent = Base64.getUrlDecoder().decode(key.e());
                            return createPublicKey(modulus, exponent);
                        }

                        throw new IllegalArgumentException("No matching public key found for kid: " + kid);
                    })
                    .requireIssuer("https://appleid.apple.com")
                    .requireAudience(clientId)
                    .build()
                    .parseSignedClaims(idToken);

            // Return the JWT claims
            return claimsJws.getPayload();
        } catch (SignatureException e) {
            throw new IllegalArgumentException("Invalid ID Token signature", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ID Token", e);
        }
    }

    private PublicKey createPublicKey(byte[] modulus, byte[] exponent) {
        // Implement RSA public key creation logic using the modulus and exponent
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
                new java.math.BigInteger(1, modulus),
                new java.math.BigInteger(1, exponent)
        );

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            log.error("Failed to create RSA public key on authentication", e);
            throw new RuntimeException(e);
        }
    }

}
