package ai.holo.wdyt.common.notification.service;

import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.common.notification.model.NotificationType;
import ai.holo.wdyt.common.notification.model.PushNotification;
import ai.holo.wdyt.common.notification.repository.PushNotificationRepository;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PushNotificationService {
    private final SnsClient snsClient;
    private final String snsArn;
    private final UserRepository userRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final String apnsIdentifier;

    public PushNotificationService(@Value("${aws.region}") String region,
                                   @Value("${aws.profile}") String awsProfile,
                                   @Value("${aws.sns.pushNotificationArn}") String snsArn,
                                   @Value("${aws.sns.apnsIdentifier}") String apnsIdentifier,
                                   UserRepository userRepository,
                                   PushNotificationRepository pushNotificationRepository) {
        this.snsArn = snsArn;
        this.userRepository = userRepository;
        this.apnsIdentifier = apnsIdentifier;
        this.pushNotificationRepository = pushNotificationRepository;
        this.snsClient = SnsClient.builder().
                region(Region.of(region)).
                credentialsProvider(ProfileCredentialsProvider.create(awsProfile)).build();
    }

    @Transactional
    public void sendPushNotification(Long userId, String title, String message, NotificationType notificationType) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        String deviceToken = user.getDeviceToken();
        if (StringUtils.isEmpty(deviceToken)) {
            log.warn("User {} does not have a device token, so push notification will not be sent", user.getId());
            return;
        }
        try {
            String snsEndpoint = getOrCreateValidSnsEndpoint(deviceToken);
            String content = buildPushMessage(title, message);
            sendNotification(content, snsEndpoint);
            pushNotificationRepository.save(new PushNotification(notificationType, userId, content));
            log.info("Push notification {} sent to user {}", content, user.getId());
        } catch (EndpointDisabledException e) {
            log.warn("SNS endpoint is disabled for user {}. Removing device token.", user.getId());
            removeDeviceTokenFromDisabledEndpointUser(user);
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}", user.getId(), e);
        }
    }

    private void removeDeviceTokenFromDisabledEndpointUser(User user) {
        user.setDeviceToken(null);
        user.setTimezone(null);
        userRepository.save(user);
    }

    private String getOrCreateValidSnsEndpoint(String deviceToken) {
        // TODO - Consider caching existing endpoint ARNs by token
        CreatePlatformEndpointRequest createRequest = CreatePlatformEndpointRequest.builder()
                .token(deviceToken)
                .platformApplicationArn(snsArn)
                .build();

        CreatePlatformEndpointResponse response = snsClient.createPlatformEndpoint(createRequest);
        String endpointArn = response.endpointArn();
        tryReenablingTheEndpointIfDisabled(endpointArn);
        return endpointArn;
    }

    private void tryReenablingTheEndpointIfDisabled(String endpointArn) {
        // Check if it's enabled
        GetEndpointAttributesRequest attrRequest = GetEndpointAttributesRequest.builder()
                .endpointArn(endpointArn)
                .build();

        GetEndpointAttributesResponse attrResponse = snsClient.getEndpointAttributes(attrRequest);

        if (!Boolean.parseBoolean(attrResponse.attributes().get("Enabled"))) {
            log.warn("SNS endpoint is disabled. Attempting to re-enable it.");
            Map<String, String> updatedAttrs = new HashMap<>();
            updatedAttrs.put("Enabled", "true");

            SetEndpointAttributesRequest setAttrsRequest = SetEndpointAttributesRequest.builder()
                    .endpointArn(endpointArn)
                    .attributes(updatedAttrs)
                    .build();

            snsClient.setEndpointAttributes(setAttrsRequest); // try to re-enable
        }
    }

    private void sendNotification(String message, String snsEndpoint) {
        PublishRequest request = PublishRequest.builder()
                .messageStructure("json")
                .targetArn(snsEndpoint)
                .message(message)
                .build();

        snsClient.publish(request);
    }

    private String buildPushMessage(String title, String message) {
        try {
            // Create the APNS payload structure
            String apnsPayload = String.format("{\\\"aps\\\":{\\\"alert\\\":{\\\"title\\\":\\\"%s\\\",\\\"body\\\":\\\"%s\\\"},\\\"sound\\\":\\\"default\\\"}}", title, message);

            // Wrap it in the APNS_SANDBOX key for AWS SNS

            String apnsContent = String.format("\"%s\":\"%s\"", apnsIdentifier, apnsPayload);
            return String.format("{\"default\": \"%s\",%s}", message, apnsContent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build push message JSON", e);
        }
    }
}
