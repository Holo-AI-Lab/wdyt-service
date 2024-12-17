package ai.holo.wdyt.user.controller;

import ai.holo.wdyt.user.model.dto.AddUserFeedbackDto;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.model.dto.UserFeedbackDto;
import ai.holo.wdyt.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get-info")
    public UserDto getUserInfo() {
        return userService.getUserInfo();
    }

    @PostMapping("/add-feedback")
    public UserFeedbackDto updateUserInfo(@RequestBody AddUserFeedbackDto addUserFeedbackDto) {
        return userService.addFeedback(addUserFeedbackDto);
    }

    @PostMapping("/delete-account")
    public void updateUserInfo() {
        userService.deleteAccount();
    }
}
