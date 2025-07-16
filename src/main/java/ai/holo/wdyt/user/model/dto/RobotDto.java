package ai.holo.wdyt.user.model.dto;

import ai.holo.wdyt.user.model.entity.Gender;
import ai.holo.wdyt.user.model.entity.Robot;

import java.time.ZoneOffset;

public record RobotDto(
        Long id,
        String name,
        Gender gender,
        Long birthday,
        String avatarUrl
) {
    public RobotDto(Robot robot) {
        this(
                robot.getId(),
                robot.getName(),
                robot.getGender(),
                robot.getBirthday() != null ? robot.getBirthday().toEpochSecond(ZoneOffset.UTC) : null,
                robot.getAvatarUrl()
        );
    }
}
