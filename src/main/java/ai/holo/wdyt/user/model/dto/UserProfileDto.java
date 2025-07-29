package ai.holo.wdyt.user.model.dto;

import ai.holo.wdyt.user.model.entity.Gender;
import ai.holo.wdyt.user.model.entity.User;

import java.util.List;

public record UserProfileDto(Long id,
                             String email,
                             String name,
                             String username,
                             boolean publicProfile,
                             String profilePictureUrl,
                             Long robotId,
                             String robotName,
                             Gender robotGender,
                             String robotAvatarUrl,
                             List<String> mostPreferredOccasions,
                             List<String> mostPreferredStyles,
                             List<String> mostPreferredColors,
                             int feedbacksReceived,
                             int numberOfOutfit,
                             int numberOfFriend
                             ) {

    public UserProfileDto(User user, List<String> occasions, List<String> styles, List<String> colors,
                          int feedbacksReceived, int numberOfOutfit, int numberOfFriend) {
        this(user.getId(), user.getEmail(), user.getName(), user.getUsername(), user.isPublicProfile(),
                user.getProfilePicture(), user.getRobot().getId(), user.getRobot().getName(),
                user.getRobot().getGender(), user.getRobot().getAvatarUrl(), occasions, styles,
                colors, feedbacksReceived, numberOfOutfit, numberOfFriend);
    }
}
