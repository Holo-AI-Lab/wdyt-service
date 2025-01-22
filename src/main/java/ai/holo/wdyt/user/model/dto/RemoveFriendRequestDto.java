package ai.holo.wdyt.user.model.dto;

import jakarta.validation.constraints.NotNull;

public record RemoveFriendRequestDto(@NotNull Long friendId) {
}
