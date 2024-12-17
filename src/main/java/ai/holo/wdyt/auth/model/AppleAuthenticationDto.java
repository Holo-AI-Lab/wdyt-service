package ai.holo.wdyt.auth.model;

import jakarta.validation.constraints.NotBlank;

public record AppleAuthenticationDto (@NotBlank(message = "{apple.auth.code.not.blank}") String authorizationCode) {

}
