package ai.holo.wdyt.user.model.dto;

import ai.holo.wdyt.user.model.entity.User;

public record FriendRequestDto(Long id,
                               String friendName,
                               String friendUsername,
                               String friendProfilePictureUrl) {

    public FriendRequestDto(Long friendRequestId, User user) {
        this(friendRequestId, user.getName(), user.getUsername(), user.getProfilePicture());
    }
}
