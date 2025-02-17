package ai.holo.wdyt.subscription.controller;

import ai.holo.wdyt.auth.service.AuthenticationContext;
import ai.holo.wdyt.subscription.model.dto.UserDetailedCreditsDTO;
import ai.holo.wdyt.subscription.model.dto.UserValidCreditsDTO;
import ai.holo.wdyt.subscription.service.UserCreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credit")
@Slf4j
public class UserCreditController {
    private final UserCreditService userCreditService;
    private final AuthenticationContext authenticationContext;
    public UserCreditController(UserCreditService userCreditService, AuthenticationContext authenticationContext) {
        this.userCreditService = userCreditService;
        this.authenticationContext = authenticationContext;
    }

    @GetMapping("/get")
    public UserValidCreditsDTO getTotalCredit() {
        return userCreditService.getTotalCredits(authenticationContext.getLoggedInUserId());
    }

    @GetMapping("/getDetailed")
    public List<UserDetailedCreditsDTO> getDetailedCredit() {
        return userCreditService.getDetailedCredits(authenticationContext.getLoggedInUserId());
    }
}
