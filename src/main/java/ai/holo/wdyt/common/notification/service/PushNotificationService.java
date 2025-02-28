package ai.holo.wdyt.common.notification.service;

import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.common.notification.model.NotificationType;
import ai.holo.wdyt.common.notification.model.PushNotification;
import ai.holo.wdyt.common.notification.repository.PushNotificationRepository;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.repository.UserRepository;
import ai.holo.wdyt.user.service.UserService;
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
public class PushNotificationService {
    private final SnsClient snsClient;
    private final String snsArn;
    private final UserRepository userRepository;
    private final PushNotificationRepository pushNotificationRepository;

    public PushNotificationService(@Value("${aws.region}") String region,
                                   @Value("${aws.profile}") String awsProfile,
                                   @Value("${aws.sns.pushNotificationArn}") String snsArn,
                                   UserRepository userRepository,
                                   PushNotificationRepository pushNotificationRepository) {
        this.snsArn = snsArn;
        this.userRepository = userRepository;
        this.pushNotificationRepository = pushNotificationRepository;
        this.snsClient = SnsClient.builder().
                region(Region.of(region)).
                credentialsProvider(ProfileCredentialsProvider.create(awsProfile)).build();
    }

    @Transactional
    public void sendPushNotification(Long userId, String message) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        String snsEndpoint = createSnsEndpoint(user.getDeviceToken());
        String content = buildPushMessage(String.format("Hey %s", user.getName()), message);
        sendNotification(message, snsEndpoint);

        pushNotificationRepository.save(new PushNotification(NotificationType.OTHER, userId, content));
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
        return "{ \\\"aps\\\": { \\\"alert\\\": { \\\"title\\\": \\\"" + title + "\\\", \\\"body\\\": \\\"" + message + "\\\" }, \\\"sound\\\": \\\"default\\\" } }";
    }
}
