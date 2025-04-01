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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
    private static final int DAYS_BACK = 3;
    private final ObjectMapper objectMapper;
    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final AppleSubscriptionService appleSubscriptionService;
    private final String appleNotificationHistoryApiUrl;

    public AppleNotificationHistorySyncService(@Value("${apple.storekit.url}") String appleNotificationHistoryApiUrl,
                                               ObjectMapper objectMapper,
                                               AppleClientSecretGenerator appleClientSecretGenerator,
                                               AppleSubscriptionService appleSubscriptionService) {
        this.appleNotificationHistoryApiUrl = appleNotificationHistoryApiUrl;
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
        log.info("Starting Apple notification history sync...");
        long startDate = Instant.now().minus(DAYS_BACK, ChronoUnit.DAYS).toEpochMilli();
        long endDate = Instant.now().toEpochMilli();
        boolean onlyFailures = true;
        String paginationToken = null;
        boolean hasMore;

        do {
            String requestUrl = buildRequestUrl(paginationToken);
            Map<String, Object> requestBody = buildRequestBody(paginationToken, startDate, endDate, onlyFailures);
            String response = callAppleApi(requestUrl, requestBody).block();
            System.out.println("Response: " + response);
            AppleNotificationHistoryResponse parsedModel = objectMapper.readValue(response, AppleNotificationHistoryResponse.class);
            processNotifications(parsedModel.notificationHistory());
            hasMore = parsedModel.hasMore();
            paginationToken = hasMore ? parsedModel.paginationToken() : null;
        } while (hasMore);
        log.info("Apple notification history sync completed.");
    }

    private String buildRequestUrl(String paginationToken) {
        return (paginationToken == null) ? appleNotificationHistoryApiUrl : appleNotificationHistoryApiUrl + "?paginationToken=" + paginationToken;
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

    private Mono<String> callAppleApi(String requestUrl, Map<String, Object> requestBody) throws Exception {
        String jwtToken = appleClientSecretGenerator.generateAppleJwtFromKeyString();
        return WebClient.builder()
                .baseUrl(requestUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .build()
                .post()
                .bodyValue(requestBody)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class);
                    } else {
                        log.error("Apple API returned status: {}", response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Error from Apple API: " + body)));
                    }
                });
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
