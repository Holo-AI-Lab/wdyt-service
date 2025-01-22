package ai.holo.wdyt.user.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ChangeUsernameDto(
        @NotEmpty
        @Size(min = 6, message = "{user.username.min.characters}")
        String username) {
}
