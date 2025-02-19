package ai.holo.wdyt.subscription.service;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private final String teamId;
    private final String keyId;
    private final String privateKeyString;
    public JwtUtil(Map<String, String> secretProperties) {
        this.teamId = secretProperties.get("appleTeamId");
        this.keyId = secretProperties.get("appleKeyId");
        this.privateKeyString = secretProperties.get("applePrivateKey");
    }

    public String generateAppleJwtFromKeyString() throws Exception {
        String formattedKey = privateKeyString.
                replace("-----BEGIN PRIVATE KEY-----", "").
                replace("-----END PRIVATE KEY-----", "").
                replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(formattedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(keySpec);

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).type(JOSEObjectType.JWT).build();

        long nowInSeconds = System.currentTimeMillis() / 1000;
        Date issuedAt = new Date(nowInSeconds * 1000);
        Date expiration = new Date((nowInSeconds + (20 * 60)) * 1000); // 20 minutes later

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issuer(teamId).issueTime(issuedAt).expirationTime(expiration).build();
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        JWSSigner signer = new ECDSASigner(privateKey);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
}
