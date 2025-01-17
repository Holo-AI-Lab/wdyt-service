package ai.holo.wdyt.user.controller;

import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserFriendService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friend")
public class UserFriendController {

    private final UserFriendService userFriendService;

    public UserFriendController(UserFriendService userFriendService) {
        this.userFriendService = userFriendService;
    }

    @GetMapping("/friendList")
    public PagedModel<UserDto> friendList(@RequestBody User user, @RequestParam(defaultValue = "100") Integer size,
                                          @RequestParam(defaultValue = "0") Integer page) {
        return userFriendService.findFriendList(user.getUsername(), PageRequest.of(page, size));
    }

    @RequestMapping(value="")
    @GetMapping("/pendingFriendList")
    public PagedModel<UserDto> pendingFriendList(@RequestParam(defaultValue = "100") Integer size, @RequestParam(defaultValue = "0") Integer page) {
        return userFriendService.findPendingFriendList(PageRequest.of(page, size));
    }


    @PostMapping("/addFriend")
    public void addFriend(Long friendId) {
        userFriendService.addFriend(friendId);
    }


    @PostMapping("/rejectFriend")
    public void rejectFriend(Long friendId) {
        userFriendService.rejectFriend(friendId);
    }

    @PostMapping("/acceptFriend")
    public void acceptFriend(Long friendId) {
        userFriendService.acceptFriend(friendId);
    }

}
