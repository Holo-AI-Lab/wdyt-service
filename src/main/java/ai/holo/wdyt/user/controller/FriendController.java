package ai.holo.wdyt.user.controller;

import ai.holo.wdyt.user.model.dto.*;
import ai.holo.wdyt.user.service.FriendService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friend")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/add-request")
    public void addFriendRequest(@RequestBody @Valid AddFriendRequestDto addFriendRequestDto) {
        friendService.addFriendRequest(addFriendRequestDto.friendId());
    }

    @PostMapping("/update-request")
    public void updateFriendRequest(@RequestBody @Valid UpdateFriendRequestDto updateFriendRequestDto) {
        friendService.updateFriendRequest(updateFriendRequestDto);
    }

    @DeleteMapping("/delete-request/{id}")
    public void deleteUser(@PathVariable Long id) {
        friendService.deleteFriendRequest(id);
    }

    @GetMapping("/get-pending-requests")
    public Page<FriendRequestDto> getPendingRequests(@RequestParam(defaultValue = "100") Integer size,
                                                     @RequestParam(defaultValue = "0") Integer page) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return friendService.getPendingRequests(pageRequest);
    }

    @GetMapping("/get-friends")
    public Page<UserDto> getFriends(@RequestParam(defaultValue = "100") Integer size,
                                    @RequestParam(defaultValue = "0") Integer page,
                                    @RequestParam(defaultValue = "friend.name") String orderBy,
                                    @RequestParam(defaultValue = "ASC") String order) {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(order.toUpperCase())
                .orElse(Sort.Direction.DESC);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, orderBy));
        return friendService.getFriends(pageRequest);
    }

    @PostMapping("/remove-friend")
    public void removeFriend(@RequestBody @Valid RemoveFriendRequestDto removeFriendRequestDto) {
        friendService.removeFriend(removeFriendRequestDto);
    }
}
