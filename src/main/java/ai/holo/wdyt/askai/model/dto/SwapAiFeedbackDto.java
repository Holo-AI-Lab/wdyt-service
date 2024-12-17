package ai.holo.wdyt.askai.model.dto;

import jakarta.validation.constraints.NotBlank;

public record SwapAiFeedbackDto(@NotBlank(message = "{ai.feedback.id.not.blank}") Long feedbackOneId,
                                @NotBlank(message = "{ai.feedback.id.not.blank}") Long feedbackTwoId) {

}