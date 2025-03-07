package ai.holo.wdyt.askai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportAiFeedbackDto(@NotBlank(message = "{user.feedback.not.blank}")
                                  @Size(max=5000, message = "{user.feedback.max.character}") String feedback,
                                  String feedbackEntryId) {
}
