package ai.holo.wdyt.askai.model.dto;

import jakarta.validation.constraints.NotBlank;

public record PinAiFeedbackDto(@NotBlank(message = "{ai.feedback.id.not.blank}") Long aiFeedbackId, boolean pin) {

}