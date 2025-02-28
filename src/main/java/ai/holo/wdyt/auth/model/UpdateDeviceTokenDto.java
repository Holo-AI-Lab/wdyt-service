package ai.holo.wdyt.auth.model;

import jakarta.validation.constraints.NotEmpty;

public record UpdateDeviceTokenDto(@NotEmpty String deviceToken) {
}
