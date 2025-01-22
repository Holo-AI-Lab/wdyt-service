package ai.holo.wdyt.user.model.dto;

import ai.holo.wdyt.user.model.entity.Gender;
import ai.holo.wdyt.user.model.entity.User;

public record UserSearchDto(Long id,
                            String email,
                            String name,
                            String username,
                            String profilePictureUrl,
                            Long robotId,
                            String robotName,
                            Gender robotGender,
                            String robotAvatarUrl,
                            boolean isStyleAdapted,
                            UserSelectedStyle selectedStyle,
                            boolean isFriend,
                            boolean hasPendingRequest) {

    public UserSearchDto(User user, boolean isFriend, boolean hasPendingRequest) {
        this(user.getId(), user.getEmail(), user.getName(), user.getUsername(),
                user.getProfilePicture(), user.getRobot().getId(), user.getRobot().getName(),
                user.getRobot().getGender(), user.getRobot().getAvatarUrl(), user.isStyleAdapted(),
                user.getSelectedStyle(), isFriend, hasPendingRequest);
    }
}
