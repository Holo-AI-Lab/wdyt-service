package ai.holo.wdyt.user.service;

import ai.holo.wdyt.user.enums.FriendEnum;
import ai.holo.wdyt.user.model.dto.UserDto;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.model.entity.UserFriend;
import ai.holo.wdyt.user.model.entity.UserFriendRecord;
import ai.holo.wdyt.user.repository.UserFriendRecordRepository;
import ai.holo.wdyt.user.repository.UserFriendRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class UserFriendService {
    private final UserService userService;

    private final UserFriendRepository userFriendRepository;

    private final UserFriendRecordRepository userFriendRecordRepository;

    public UserFriendService(UserService userService, UserFriendRepository userFriendRepository,
                             UserFriendRecordRepository userFriendRecordRepository) {
        this.userService = userService;
        this.userFriendRepository = userFriendRepository;
        this.userFriendRecordRepository = userFriendRecordRepository;
    }

    public PagedModel<UserDto> findFriendList(String username, PageRequest pageRequest) {
        Page<User> page = userFriendRepository.findFriendWithUser(userService.getUser().getId(), FriendEnum.FRIEND.getStatus(), username, pageRequest);
        return new PagedModel<>(new PageImpl<>(page.getContent().stream().map(UserDto::new).toList(), pageRequest, page.getTotalElements()));
    }

    public PagedModel<UserDto> findPendingFriendList(PageRequest pageRequest) {
        Page<User> page = userFriendRepository.findByUserRIdAndStatus(userService.getUser().getId(), FriendEnum.PENDING.getStatus(), pageRequest);
        return new PagedModel<>(new PageImpl<>(page.getContent().stream().map(UserDto::new).toList(), pageRequest, page.getTotalElements()));
    }

    @Transactional
    public void addFriend(Long friendId) {
        if (friendId == null) {
            return;
        }
        Long userId = userService.getUser().getId();
        UserFriend userFriend = new UserFriend();
        userFriend.setUserLId(userId);
        userFriend.setUserRId(friendId);
        userFriend.setUserLRId(userId + "x" + friendId);
        userFriend.setStatus(FriendEnum.PENDING.getStatus());
        userFriendRepository.save(userFriend);
        saveFriendRecord(userFriend.getUserLId(), userFriend.getUserRId(), FriendEnum.PENDING.getStatus());
    }

    @Transactional
    public void rejectFriend(Long friendId) {
        if (friendId == null) {
            return;
        }
        Long userId = userService.getUser().getId();
        UserFriend userFriend = userFriendRepository.findByUserLRIdAndStatus(friendId + "x" + userId, FriendEnum.PENDING.getStatus());
        if (null != userFriend) {
            userFriendRepository.delete(userFriend);
            saveFriendRecord(userFriend.getUserRId(), userFriend.getUserLId(), FriendEnum.REJECTED.getStatus());
        }
    }

    @Transactional
    public void acceptFriend(Long friendId) {
        if (friendId == null) {
            return;
        }
        Long userId = userService.getUser().getId();
        UserFriend userFriend = userFriendRepository.findByUserLRIdAndStatus(friendId + "x" + userId, FriendEnum.PENDING.getStatus());
        if (null != userFriend) {
            userFriend.setStatus(FriendEnum.FRIEND.getStatus());
            userFriendRepository.save(userFriend);
            saveFriendRecord(userFriend.getUserRId(), userFriend.getUserLId(), FriendEnum.FRIEND.getStatus());
        }
    }

    public void saveFriendRecord(Long userId, Long friendId, String status) {
        UserFriendRecord userFriend = new UserFriendRecord();
        userFriend.setStatus(status);
        userFriend.setUserId(userId);
        userFriend.setFriendId(friendId);
        userFriend.setCreateDate(new Date());
        userFriendRecordRepository.save(userFriend);
    }

}
