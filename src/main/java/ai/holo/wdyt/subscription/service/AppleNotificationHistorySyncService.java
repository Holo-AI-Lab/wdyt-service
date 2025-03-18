package ai.holo.wdyt.subscription.service;

import ai.holo.wdyt.config.authentication.apple.AppleClientSecretGenerator;
import ai.holo.wdyt.subscription.model.dto.AppleNotificationHistoryResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AppleNotificationHistorySyncService {

    private static final String APPLE_API_URL = "https://api.storekit-sandbox.itunes.apple.com/inApps/v1/notifications/history";
    // For Production:
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
    @Scheduled(cron = "0 0 3 * * ?")
    @SchedulerLock(name = "Scheduler_AppleNotificationHistorySyncLock", lockAtLeastFor = "PT5M", lockAtMostFor = "PT55M")
    @Transactional
    public void syncNotificationHistory() throws Exception {
        long startDate = Instant.now().minus(DAYS_BACK, ChronoUnit.DAYS).toEpochMilli();
        long endDate = Instant.now().toEpochMilli();
        boolean onlyFailures = true;
        String paginationToken = null;
        boolean hasMore;

        do {
                String requestUrl = buildRequestUrl(paginationToken);
                Map<String, Object> requestBody = buildRequestBody(paginationToken, startDate, endDate, onlyFailures);
                ResponseEntity<String> response = callAppleApi(requestUrl, requestBody);
                AppleNotificationHistoryResponse parsedModel = objectMapper.readValue(response.getBody(), AppleNotificationHistoryResponse.class);
                processNotifications(parsedModel.notificationHistory());
                hasMore = parsedModel.hasMore();
                paginationToken = hasMore ? parsedModel.paginationToken() : null;
        } while (hasMore);
    }

    private String buildRequestUrl(String paginationToken) {
        return (paginationToken == null) ? APPLE_API_URL : APPLE_API_URL + "?paginationToken=" + paginationToken;
    }

    private Map<String, Object> buildRequestBody(String paginationToken, long startDate, long endDate, boolean onlyFailures) {
        if (paginationToken != null) {
            return Collections.emptyMap();
        }
        return Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "onlyFailures", onlyFailures
        );
    }

    private ResponseEntity<String> callAppleApi(String requestUrl, Map<String, Object> requestBody) throws Exception {
        String jwtToken = appleClientSecretGenerator.generateAppleJwtFromKeyString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        return restTemplate.exchange(
                requestUrl, HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );
    }

    private void processNotifications(List<AppleNotificationHistoryResponse.AppleNotificationItem> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            log.info("No notifications found in the response.");
            return;
        }
        for (AppleNotificationHistoryResponse.AppleNotificationItem notification : notifications) {
            try {
                appleSubscriptionService.processNotification(notification.signedPayload());
                log.info("Notification processed successfully: {}", notification.signedPayload());
            } catch (Exception ex) {
                log.error("Error processing notification with token {}: {} -(From historySyncService)", notification.signedPayload(), ex.getMessage());
            }
        }
    }

    public static class AppleNotificationHistoryDeserializer extends StdDeserializer<AppleNotificationHistoryResponse> {
        protected AppleNotificationHistoryDeserializer() {
            super(AppleNotificationHistoryResponse.class);
        }

        @Override
        public AppleNotificationHistoryResponse deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            JsonNode rootNode = parser.getCodec().readTree(parser);
            boolean hasMore = getBoolean(rootNode, "hasMore", false);
            String paginationToken = getText(rootNode, "paginationToken");
            List<AppleNotificationHistoryResponse.AppleNotificationItem> items = parseNotifications(rootNode.get("notificationHistory"));
            return new AppleNotificationHistoryResponse(hasMore, paginationToken, items);
        }

        private List<AppleNotificationHistoryResponse.AppleNotificationItem> parseNotifications(JsonNode node) {
            if (node == null || !node.isArray()) {
                return Collections.emptyList();
            }
            List<AppleNotificationHistoryResponse.AppleNotificationItem> list = new ArrayList<>();
            for (JsonNode itemNode : node) {
                String signedPayload = getText(itemNode, "signedPayload");
                if (signedPayload != null) {
                    list.add(new AppleNotificationHistoryResponse.AppleNotificationItem(signedPayload));
                }
            }
            return list;
        }

        private String getText(JsonNode rootNode, String fieldName) {
            try {
                return rootNode.get(fieldName).asText();
            } catch (Exception ignored) {
            }
            return null;
        }

        private boolean getBoolean(JsonNode rootnode, String fieldName, boolean defaultVal) {
            if (rootnode == null) return defaultVal;
            JsonNode boolNode = rootnode.get(fieldName);
            return (boolNode != null && boolNode.isBoolean()) ? boolNode.asBoolean() : defaultVal;
        }
    }
}
