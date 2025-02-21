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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final AppleSubscriptionService appleSubscriptionService;

    // For production:
    // private final String appleApiUrl = "https://api.storekit.itunes.apple.com/inApps/v1/notifications/history";
    // For sandbox:
    private final String appleApiUrl = "https://api.storekit-sandbox.itunes.apple.com/inApps/v1/notifications/history";

    public AppleNotificationHistorySyncService(ObjectMapper objectMapper,
                                               AppleClientSecretGenerator appleClientSecretGenerator,
                                               AppleSubscriptionService appleSubscriptionService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.appleClientSecretGenerator = appleClientSecretGenerator;
        this.appleSubscriptionService = appleSubscriptionService;
    }

    // Schedule for every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void syncNotificationHistory() {
        long startDate = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        long endDate = Instant.now().toEpochMilli();
        boolean onlyFailures = true;
        String paginationToken = null;
        boolean hasMore = false;

        do {
            try {
                String requestUrl = appleApiUrl;
                Map<String, Object> body = new HashMap<>();
                if (paginationToken == null) {
                    // First request: include the full parameters in the body.
                    body.put("startDate", startDate);
                    body.put("endDate", endDate);
                    body.put("onlyFailures", onlyFailures);
                } else {
                    // Subsequent requests: send only the paginationToken as a query parameter with an empty body.
                    requestUrl = appleApiUrl + "?paginationToken=" + paginationToken;
                }

                String jwtToken = appleClientSecretGenerator.generateAppleJwtFromKeyString();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(jwtToken);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.POST, entity, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.error("Apple API returned status: {}", response.getStatusCode());
                    throw new RuntimeException("Apple API returned status: " + response.getStatusCode());
                }

                JsonNode rootNode = objectMapper.readTree(response.getBody());

                if (rootNode.has("notificationHistory") && rootNode.get("notificationHistory").isArray()) {
                    for (JsonNode notificationNode : rootNode.get("notificationHistory")) {
                        if (notificationNode.has("signedPayload")) {
                            String jwsToken = notificationNode.get("signedPayload").asText();
                            try {
                                appleSubscriptionService.processNotification(jwsToken);
                                log.info("Notification processed successfully: {} (from getHistory)", jwsToken);
                            } catch (Exception ex) {
                                log.error("Error processing notification with token {}: {}", jwsToken, ex.getMessage());
                            }
                        } else {
                            log.warn("Notification item missing 'signedPayload' field: {}", notificationNode);
                        }
                    }
                } else {
                    log.info("No notifications found in the response.");
                }

                hasMore = rootNode.path("hasMore").asBoolean(false);
                if (hasMore) {
                    paginationToken = rootNode.path("paginationToken").asText();
                    log.info("Pagination token found. Fetching next page: {}", paginationToken);
                } else {
                    paginationToken = null;
                }
            } catch (Exception e) {
                log.error("Error during notification history sync", e);
                throw new RuntimeException("Error during notification history sync: " + e.getMessage(), e);
            }
        } while (hasMore);
    }
}
