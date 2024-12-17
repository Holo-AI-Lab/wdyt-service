package ai.holo.wdyt.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApplePublicKeyCache {
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    private final WebClient webClient;

    private final ConcurrentHashMap<String, List<ApplePublicKey>> cache = new ConcurrentHashMap<>();

    private Instant cacheExpiry = Instant.MIN;

    // Cache duration - 24 hours
    private static final long CACHE_DURATION_SECONDS = 24 * 60 * 60;

    public ApplePublicKeyCache(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(APPLE_KEYS_URL).build();
    }

    /**
     * Fetch Apple public keys, using the cache if still valid.
     */
    public List<ApplePublicKey> getCachedAppleKeys() {
        if (isCacheValid()) {
            return cache.get("appleKeys");
        }

        List<ApplePublicKey> applePublicKeys = fetchApplePublicKeys();

        // Update cache
        cache.put("appleKeys", applePublicKeys);
        cacheExpiry = Instant.now().plusSeconds(CACHE_DURATION_SECONDS);

        return applePublicKeys;
    }

    private List<ApplePublicKey> fetchApplePublicKeys() {
        ApplePublicKeysResponse applePublicKeysResponse = webClient.get()
                .retrieve()
                .bodyToMono(ApplePublicKeysResponse.class)
                .block();
        return applePublicKeysResponse != null ? applePublicKeysResponse.keys() : List.of();
    }

    private boolean isCacheValid() {
        return Instant.now().isBefore(cacheExpiry) && cache.containsKey("appleKeys");
    }

    public record ApplePublicKeysResponse(List<ApplePublicKey> keys) {
    }
    public record ApplePublicKey (String kty, String kid, String use, String alg, String n, String e) {
    }
}
