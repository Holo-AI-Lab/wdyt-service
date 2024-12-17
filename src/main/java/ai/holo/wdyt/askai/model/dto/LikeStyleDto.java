package ai.holo.wdyt.askai.model.dto;

import jakarta.validation.constraints.NotBlank;

public record LikeStyleDto(@NotBlank(message = "{ai.feedback.id.not.blank}") Long id, boolean like) {

}