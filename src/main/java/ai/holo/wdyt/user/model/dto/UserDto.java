package ai.holo.wdyt.user.model.dto;

import ai.holo.wdyt.user.model.entity.Gender;
import ai.holo.wdyt.user.model.entity.User;

public record UserDto(Long id,
                      String email,
                      String name,
                      Long robotId,
                      String robotName,
                      Gender robotGender,
                      String robotAvatarUrl) {

    public UserDto(User user) {
        this(user.getId(), user.getEmail(), user.getName(),
                user.getRobot().getId(), user.getRobot().getName(),
                user.getRobot().getGender(), user.getRobot().getAvatarUrl());
    }
}
