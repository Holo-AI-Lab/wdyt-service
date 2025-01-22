package ai.holo.wdyt.user.model.dto;

import ai.holo.wdyt.user.model.entity.User;

public record FriendRequestDto(Long id,
                               String friendName,
                               String friendUsername,
                               String friendProfilePictureUrl) {

    public FriendRequestDto(User user) {
        this(user.getId(), user.getName(), user.getUsername(), user.getProfilePicture());
    }
}
