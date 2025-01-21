package ai.holo.wdyt.user.model.dto;

import jakarta.validation.constraints.NotEmpty;

public record ChangeNameDto(@NotEmpty String name) {
}
