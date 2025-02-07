package ai.holo.wdyt.subscription.controller;

import ai.holo.wdyt.subscription.model.dto.UserSubscriptionDto;
import ai.holo.wdyt.subscription.model.dto.UserTransactionDto;
import ai.holo.wdyt.subscription.service.AppleSubscriptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AppleSubscriptionController {

    private final AppleSubscriptionService appleSubscriptionService;

    public AppleSubscriptionController(AppleSubscriptionService appleSubscriptionService) {
        this.appleSubscriptionService = appleSubscriptionService;
    }

    @PostMapping("/initiate-subscription")
    public UserSubscriptionDto initiateSubscription() {
        return appleSubscriptionService.initiateSubscription();
    }

    @PostMapping("/notify-transaction")
    public void notifyTransaction(@RequestBody UserTransactionDto userSubscriptionDto) {
        appleSubscriptionService.createTransaction(userSubscriptionDto);
    }

    @PostMapping("/notification")
    public void handleAppleNotification(@RequestBody String jwsToken) {
        appleSubscriptionService.processNotification(jwsToken);
    }
}
