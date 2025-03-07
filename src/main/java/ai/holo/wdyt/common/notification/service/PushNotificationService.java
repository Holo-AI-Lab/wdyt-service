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
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointRequest;
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;

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
        String snsEndpoint = createSnsEndpoint(deviceToken);
        String content = buildPushMessage(title, message);
        log.info("Push notification content: {}", content);
        sendNotification(content, snsEndpoint);
        pushNotificationRepository.save(new PushNotification(notificationType, userId, content));
        log.info("Push notification sent to user {}", user.getId());
    }

    private String createSnsEndpoint(String userDeviceToken) {
        CreatePlatformEndpointRequest request = CreatePlatformEndpointRequest.builder()
                .token(userDeviceToken)
                .platformApplicationArn(snsArn)
                .build();

        CreatePlatformEndpointResponse response = snsClient.createPlatformEndpoint(request);
        return response.endpointArn();
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
