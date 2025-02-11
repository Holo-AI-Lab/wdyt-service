package ai.holo.wdyt.subscription.controller;

import ai.holo.wdyt.subscription.service.UserCreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credit")
@Slf4j
public class UserCreditController {
    private final UserCreditService userCreditService;
    public UserCreditController(UserCreditService userCreditService) {
        this.userCreditService = userCreditService;
    }

    @GetMapping("/get")
    public int getTotalCredit(@RequestBody Long userId) {
        return userCreditService.getTotalCredits(userId);
    }

    // If detailed credit information is required, I can add an endpoint for this like (/creditInfoList)

}
