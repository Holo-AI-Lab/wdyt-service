package ai.holo.wdyt.user.model.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateFriendRequestDto(@NotNull Long requestId, boolean accept) {
}
