package ai.holo.wdyt.user.controller;

import ai.holo.wdyt.user.model.dto.*;
import ai.holo.wdyt.user.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    @PostMapping("/upload-profile-picture")
    public UserDto uploadProfilePicture(@RequestParam("image") MultipartFile image) throws IOException {
        return userService.uploadProfilePicture(image.getBytes());
    }

    @PostMapping("/change-username")
    public UserDto changeUsername(@RequestBody ChangeUsernameDto changeUsernameDto) {
        return userService.changeUsername(changeUsernameDto);
    }

    @PostMapping("/update-adapted-style-mode")
    public UserDto updateAdaptedStyleMode(@RequestBody UpdateAdaptedStyleModeDto updateAdaptedStyleModeDto) {
        return userService.updateAdaptedStyleMode(updateAdaptedStyleModeDto);
    }

    @PostMapping("/update-selected-styles")
    public UserDto updateSelectedStyles(@RequestBody UpdateUserSelectedStyle updateUserSelectedStyle) {
        return userService.updateUserSelectedStyles(updateUserSelectedStyle);
    }

    @GetMapping("/get-styles")
    public List<String> getStyles(@RequestParam(value = "filter", required = false) String filter) {
        return userService.getStyles(filter);
    }
}
