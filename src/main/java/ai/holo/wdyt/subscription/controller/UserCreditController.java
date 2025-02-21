package ai.holo.wdyt.subscription.controller;

import ai.holo.wdyt.subscription.model.dto.UserValidCreditsDTO;
import ai.holo.wdyt.subscription.service.UserCreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
    public UserValidCreditsDTO getTotalCredit() {
        return userCreditService.getCredit();
    }
}
