package ai.holo.wdyt.askai.model.dto;

import jakarta.validation.constraints.NotBlank;

public record UnpinAiFeedbackDto(@NotBlank(message = "{ai.feedback.id.not.blank}") Long aiFeedbackId) {

}