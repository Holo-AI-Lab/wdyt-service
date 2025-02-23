package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.config.authentication.apple.AppleClientSecretGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AppleNotificationHistorySyncService {

    private static final String APPLE_API_URL = "https://api.storekit-sandbox.itunes.apple.com/inApps/v1/notifications/history";
    // For production, use the following URL instead
    // private static final String APPLE_API_URL = "https://api.storekit.itunes.apple.com/inApps/v1/notifications/history";

    private static final int DAYS_BACK = 3;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final AppleSubscriptionService appleSubscriptionService;

    public AppleNotificationHistorySyncService(ObjectMapper objectMapper,
                                               AppleClientSecretGenerator appleClientSecretGenerator,
                                               AppleSubscriptionService appleSubscriptionService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.appleClientSecretGenerator = appleClientSecretGenerator;
        this.appleSubscriptionService = appleSubscriptionService;
    }

    /**
     * Runs every day at midnight. Fetches and processes Apple's notification history for the last 3 day
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void syncNotificationHistory() {
        long startDate = Instant.now().minus(DAYS_BACK, ChronoUnit.DAYS).toEpochMilli();
        long endDate = Instant.now().toEpochMilli();
        boolean onlyFailures = true;
        String paginationToken = null;
        boolean hasMore;

        do {
            try {
                String requestUrl = buildRequestUrl(paginationToken);
                Map<String, Object> body = buildRequestBody(paginationToken, startDate, endDate, onlyFailures);
                JsonNode responseRoot = callAppleApi(requestUrl, body);

                processNotifications(responseRoot);

                // Check for further pagination pages
                hasMore = responseRoot.path("hasMore").asBoolean(false);
                if (hasMore) {
                    paginationToken = responseRoot.path("paginationToken").asText();
                } else {
                    paginationToken = null;
                }
            } catch (Exception e) {
                log.error("Error during notification history sync", e);
                throw new RuntimeException("Error during notification history sync: " + e.getMessage());
            }
        } while (hasMore);
    }

    private String buildRequestUrl(String paginationToken) {
        if (paginationToken == null) {
            return APPLE_API_URL;
        }
        // For pagination, we add the token to the query string
        return APPLE_API_URL + "?paginationToken=" + paginationToken;
    }

    private Map<String, Object> buildRequestBody(String paginationToken,
                                                 long startDate,
                                                 long endDate,
                                                 boolean onlyFailures) {
        if (paginationToken != null) {
            // If a token is provided, Apple only expects the pagination token in the query param, so we return an empty body.
            return new HashMap<>();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("startDate", startDate);
        body.put("endDate", endDate);
        body.put("onlyFailures", onlyFailures);
        return body;
    }

    private JsonNode callAppleApi(String requestUrl, Map<String, Object> body) throws Exception {
        String jwtToken = appleClientSecretGenerator.generateAppleJwtFromKeyString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Apple API returned status: {}", response.getStatusCode());
            throw new RuntimeException("Apple API returned status: " + response.getStatusCode());
        }
        return objectMapper.readTree(response.getBody());
    }

    private void processNotifications(JsonNode rootNode) {
        JsonNode notificationHistoryNode = rootNode.path("notificationHistory");
        if (!notificationHistoryNode.isArray()) {
            log.info("No notifications found in the response.");
            return;
        }
        notificationHistoryNode.forEach(notificationNode -> {
            if (!notificationNode.has("signedPayload")) {
                log.warn("Notification item missing 'signedPayload' field: {}", notificationNode);
                return;
            }

            String jwsToken = notificationNode.get("signedPayload").asText();
            try {
                appleSubscriptionService.processNotification(jwsToken);
                log.info("Notification processed successfully (from getHistory): {}", jwsToken);
            } catch (Exception ex) {
                log.error("Error processing notification with token {}: {}", jwsToken, ex.getMessage());
            }
        });
    }
}
