package ai.holo.wdyt.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddUserFeedbackDto(@NotBlank(message = "{user.feedback.not.blank}")
                                 @Size(max=5000, message = "{user.feedback.max.character}") String feedback) {

}
