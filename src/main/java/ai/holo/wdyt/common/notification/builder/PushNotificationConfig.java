package ai.holo.wdyt.common.notification.builder;

import ai.holo.wdyt.common.notification.service.PushNotificationService;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationConfig {
    public PushNotificationConfig(PushNotificationService pushNotificationService) {
        PushNotificationBuilder.setPushNotificationService(pushNotificationService);
    }
}