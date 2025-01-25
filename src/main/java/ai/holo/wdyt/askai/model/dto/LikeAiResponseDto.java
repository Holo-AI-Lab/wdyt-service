package ai.holo.wdyt.askai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LikeAiResponseDto(@NotNull(message = "{ai.feedback.id.not.blank}") Long id, @NotBlank(message = "{ai.feedback.entry.id.not.blank}")  String feedbackId, boolean like) {

}