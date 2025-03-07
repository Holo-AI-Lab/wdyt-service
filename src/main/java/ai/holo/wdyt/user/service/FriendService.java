package ai.holo.wdyt.user.service;

import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.common.notification.builder.PushNotificationBuilder;
import ai.holo.wdyt.common.notification.model.NotificationType;
import ai.holo.wdyt.user.model.dto.FriendRequestDto;
import ai.holo.wdyt.user.model.dto.RemoveFriendRequestDto;
import ai.holo.wdyt.user.model.dto.UpdateFriendRequestDto;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.model.entity.Friend;
import ai.holo.wdyt.user.model.entity.FriendRequest;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.repository.FriendRepository;
import ai.holo.wdyt.user.repository.FriendRequestRepository;
import ai.holo.wdyt.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FriendService {
    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    public FriendService(FriendRepository friendRepository, FriendRequestRepository friendRequestRepository, UserService userService, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public void deleteUser(Long id) {
        friendRequestRepository.deleteAllByUserId(id);
        friendRequestRepository.deleteAllByFriendId(id);
        friendRepository.deleteAllByUserId(id);
        friendRepository.deleteAllByFriendId(id);
    }

    public void addFriendRequest(Long friendId) {
        User user = userService.getUser();
        checkValidFriendId(friendId, user);
        checkSameFriendRequestNotExisting(friendId, user);
        checkNotAlreadyFriend(friendId, user);
        FriendRequest friendRequest = new FriendRequest(user.getId(), friendId);
        sendPushNotification(friendId, user.getName());
        friendRequestRepository.save(friendRequest);
    }

    private void sendPushNotification(Long friendId, String userName){
        PushNotificationBuilder.builder(NotificationType.FRIEND_REQUEST)
                .userId(friendId)
                .title("You’ve Got a Friend Request!")
                .message(String.format("%s sent you a friend request.", userName))
                .sendNotification();
    }

    private void checkNotAlreadyFriend(Long friendId, User user) {
        friendRepository.findByUserIdAndFriendId(user.getId(), friendId)
                .ifPresent(friend -> {
                    throw new BadRequestException("Friend already exists");
                });
    }

    private void checkValidFriendId(Long friendId, User user) {
        if (user.getId().equals(friendId)) {
            throw new BadRequestException("Cannot add yourself as a friend");
        }
    }

    private void checkSameFriendRequestNotExisting(Long friendId, User user) {
        friendRequestRepository.findByUserIdAndFriendId(user.getId(), friendId)
                .ifPresent(friendRequest -> {
                    throw new BadRequestException("Friend request already exists");
                });
    }

    public void updateFriendRequest(UpdateFriendRequestDto updateFriendRequestDto) {
        FriendRequest friendRequest = friendRequestRepository.findById(updateFriendRequestDto.requestId())
                .orElseThrow(NotFoundException::new);
        if (updateFriendRequestDto.accept()) {
            acceptFriendRequest(friendRequest);
        }
        friendRequestRepository.delete(friendRequest);
        // Delete the reverse request if it exists
        friendRequestRepository.deleteByUserIdAndFriendId(friendRequest.getFriendId(), friendRequest.getUserId());
    }

    private void acceptFriendRequest(FriendRequest friendRequest) {
        createFriends(friendRequest.getUserId(), friendRequest.getFriendId());
    }

    @Transactional
    public boolean createFriends(Long userId, Long friendId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        User friend = userRepository.findById(friendId).orElseThrow(NotFoundException::new);
        boolean alreadyFriends = true;
        if (friendRepository.findByUserIdAndFriendId(user.getId(), friend.getId()).isEmpty()) {
            friendRepository.save(new Friend(user.getId(), friend));
            alreadyFriends = false;
        }
        if (friendRepository.findByUserIdAndFriendId(friend.getId(), user.getId()).isEmpty()) {
            friendRepository.save(new Friend(friend.getId(), user));
            alreadyFriends = false;
        }
        return alreadyFriends;
    }

    public void deleteFriendRequest(Long id) {
        friendRequestRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<FriendRequestDto> getPendingRequests(PageRequest pageRequest) {
        User user = userService.getUser();
        Page<FriendRequest> friendRequests = friendRequestRepository.findAllByFriendId(user.getId(), pageRequest);
        List<Long> userIds = friendRequests.stream()
                .map(FriendRequest::getUserId)
                .toList();

        List<User> users = userRepository.findAllById(userIds);
        // Execute following logic to preserve the order of the users
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, self -> self));
        List<FriendRequestDto> friendRequestDtos = friendRequests.getContent().stream()
                .map(friendRequest -> new FriendRequestDto(friendRequest.getId(), userMap.get(friendRequest.getUserId())))
                .toList();

        return new PageImpl<>(friendRequestDtos, pageRequest, friendRequests.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getFriends(PageRequest pageRequest) {
        User user = userService.getUser();
        return friendRepository.findAllByUserId(user.getId(), pageRequest).map(it -> new UserDto(it.getFriend()));
    }

    public void removeFriend(RemoveFriendRequestDto removeFriendRequestDto) {
        User user = userService.getUser();
        Friend friend = friendRepository.findByUserIdAndFriendId(user.getId(), removeFriendRequestDto.friendId()).orElseThrow(NotFoundException::new);
        friendRepository.delete(friend);
    }
}
