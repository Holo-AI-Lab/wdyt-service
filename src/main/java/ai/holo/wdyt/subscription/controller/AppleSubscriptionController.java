package ai.holo.wdyt.subscription.controller;

import ai.holo.wdyt.subscription.service.AppleSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification")
public class AppleSubscriptionController {

    private final AppleSubscriptionService appleSubscriptionService;

    public AppleSubscriptionController(AppleSubscriptionService appleSubscriptionService) {
        this.appleSubscriptionService = appleSubscriptionService;
    }

    @PostMapping
    public ResponseEntity<?> handleAppleNotification(@RequestBody String jwsToken) {
        try {
            appleSubscriptionService.processNotification(jwsToken);
            return ResponseEntity.ok("Notification received");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error");
        }
    }
}
