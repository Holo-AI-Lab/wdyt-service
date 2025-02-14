package ai.holo.wdyt.subscription.controller;

import ai.holo.wdyt.subscription.model.dto.UserValidCreditsDTO;
import ai.holo.wdyt.subscription.service.UserCreditService;
import ai.holo.wdyt.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/credit")
@Slf4j
public class UserCreditController {
    private final UserCreditService userCreditService;
    private final UserService userService;
    public UserCreditController(UserCreditService userCreditService, UserService userService) {
        this.userCreditService = userCreditService;
        this.userService = userService;
    }

    @GetMapping("/get")
    public UserValidCreditsDTO getTotalCredit() {
        return userCreditService.getTotalCredits(userService.getLoggedInUserId());
    }

    // If detailed credit information is required, I can add an endpoint for this like (/creditInfoList)

}
