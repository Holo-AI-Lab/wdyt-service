package ai.holo.wdyt.subscription.controller;

import ai.holo.wdyt.subscription.model.dto.TransactionPendingDTO;
import ai.holo.wdyt.subscription.model.dto.UserSubscriptionDto;
import ai.holo.wdyt.subscription.model.dto.UserTransactionDto;
import ai.holo.wdyt.subscription.service.AppleSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscription")
@Slf4j
public class AppleSubscriptionController {

    private final AppleSubscriptionService appleSubscriptionService;

    public AppleSubscriptionController(AppleSubscriptionService appleSubscriptionService) {
        this.appleSubscriptionService = appleSubscriptionService;
    }

    @GetMapping("/user-subscription")
    public UserSubscriptionDto getUserSubscription() {
        return appleSubscriptionService.getUserSubscription();
    }

    @PostMapping("/initiate-subscription")
    public UserSubscriptionDto initiateSubscription() {
        return appleSubscriptionService.initiateSubscription();
    }


    @PostMapping("/set-transaction-pending")
    public UserSubscriptionDto handlePendingNotification(@RequestBody TransactionPendingDTO pendingDTO) {
        return appleSubscriptionService.setTransactionPending(pendingDTO);
    }

    @PostMapping("/notify-transaction")
    public void notifyTransaction(@RequestBody UserTransactionDto userSubscriptionDto) {
        appleSubscriptionService.createTransaction(userSubscriptionDto, true);
    }

    @PostMapping("/notification")
    public void handleAppleNotification(@RequestBody Map<String, String> jwsToken) {
        String token = jwsToken.get("signedPayload");
        if (StringUtils.isEmpty(token)) {
            log.error("signedPayload is missing");
            throw new IllegalArgumentException("signedPayload is missing");
        }
        appleSubscriptionService.processNotification(token);
    }
}
