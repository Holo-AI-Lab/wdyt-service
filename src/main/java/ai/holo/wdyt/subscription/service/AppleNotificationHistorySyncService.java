package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.subscription.model.dto.NotificationHistoryRequestDTO;
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
    private final JwtUtil jwtutil;
    private final AppleSubscriptionService appleSubscriptionService;

    // For production:
    // private final String appleApiUrl = "https://api.storekit.itunes.apple.com/inApps/v1/notifications/history";
    // For sandbox:
    private final String appleApiUrl = "https://api.storekit-sandbox.itunes.apple.com/inApps/v1/notifications/history";

    public AppleNotificationHistorySyncService(RestTemplate restTemplate,
                                               ObjectMapper objectMapper,
                                               JwtUtil jwtutil,
                                               AppleSubscriptionService appleSubscriptionService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.jwtutil = jwtutil;
        this.appleSubscriptionService = appleSubscriptionService;
    }

    /**
     * Retrieves the complete notification history and sends each notification directly to the processNotification method.
     * If a NotificationHistoryRequestDTO parameter is passed, its values (startDate, endDate, onlyFailures) are used.
     * Otherwise, default values are used (startDate = yesterday, endDate = today, onlyFailures = default value).
     * The initial request is made with the full body; for subsequent requests, only the paginationToken is sent as a query parameter.
     * When hasMore is false, all notifications have been retrieved.
     * The processNotification method handles the signedPayload (saving to the database, publishing events, etc.).
     * This method is scheduled to run daily at midnight.
     * @param request a NotificationHistoryRequestDTO (containing startDate, endDate, and onlyFailures), or null to use default values.
     */

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void syncNotificationHistory(NotificationHistoryRequestDTO request) {
        long startDate;
        long endDate;
        boolean onlyFailures;
        if (request == null) {
            startDate = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
            endDate = Instant.now().toEpochMilli();
            onlyFailures = true;
        } else {
            startDate = request.startDate();
            endDate = request.endDate();
            onlyFailures = request.onlyFailures();
        }

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

                String jwtToken = jwtutil.generateAppleJwtFromKeyString();
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
