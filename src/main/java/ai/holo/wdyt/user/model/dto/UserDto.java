package ai.holo.wdyt.user.model.dto;

import ai.holo.wdyt.user.model.entity.Gender;
import ai.holo.wdyt.user.model.entity.User;

public record UserDto(Long id,
                      String email,
                      String name,
                      String username,
                      String profilePictureUrl,
                      Long robotId,
                      String robotName,
                      Gender robotGender,
                      String robotAvatarUrl,
                      boolean publicProfile,
                      boolean isStyleAdapted,
                      UserSelectedStyle selectedStyle) {

    public static final long INACTIVE_DUMMY_USER_ID = -1L;

    public UserDto(User user) {
        this(user.getId(), user.getEmail(), user.getName(), user.getUsername(),
                user.getProfilePicture(), user.getRobot().getId(), user.getRobot().getName(),
                user.getRobot().getGender(), user.getRobot().getAvatarUrl(), user.isPublicProfile(),
                user.isStyleAdapted(), user.getSelectedStyle());
    }

    public static UserDto inactiveDummyUserDto() {
        return new UserDto(INACTIVE_DUMMY_USER_ID, null, null, null, null,
                null, null, null, null, false, false, null);
    }
}
