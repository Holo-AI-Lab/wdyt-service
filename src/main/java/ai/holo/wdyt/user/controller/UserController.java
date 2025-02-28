package ai.holo.wdyt.user.controller;

import ai.holo.wdyt.user.model.dto.*;
import ai.holo.wdyt.user.service.UserDeleteAccountService;
import ai.holo.wdyt.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final UserDeleteAccountService userDeleteAccountService;

    public UserController(UserService userService, UserDeleteAccountService userDeleteAccountService) {
        this.userService = userService;
        this.userDeleteAccountService = userDeleteAccountService;
    }

    @GetMapping("/get-info")
    public UserDto getUserInfo() {
        return userService.getUserInfo();
    }

    @GetMapping("/search-users")
    public Page<UserSearchDto> searchUsers(@RequestParam(value = "userName") String userName,
                                     @RequestParam(defaultValue = "100") Integer size,
                                     @RequestParam(defaultValue = "0") Integer page) {
        return userService.searchUsers(userName, PageRequest.of(page, size));
    }

    @PostMapping("/add-feedback")
    public UserFeedbackDto updateUserInfo(@RequestBody AddUserFeedbackDto addUserFeedbackDto) {
        return userService.addFeedback(addUserFeedbackDto);
    }

    @PostMapping("/delete-account")
    public void deleteAccount() {
        userDeleteAccountService.deleteAccount();
    }

    @PostMapping("/upload-profile-picture")
    public UserDto uploadProfilePicture(@RequestParam("image") MultipartFile image) throws IOException {
        return userService.uploadProfilePicture(image.getBytes());
    }

    @PostMapping("/change-username")
    public UserDto changeUsername(@RequestBody @Valid ChangeUsernameDto changeUsernameDto) {
        return userService.changeUsername(changeUsernameDto);
    }

    @PostMapping("/change-name")
    public UserDto changeName(@RequestBody @Valid ChangeNameDto changeNameDto) {
        return userService.changeName(changeNameDto);
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

    public void sendHellowWorldPushNotification() {
        userService.sendHelloWorldPushNotification();
    }
}
