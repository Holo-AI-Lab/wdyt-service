package ai.holo.wdyt.subscription.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserValidCreditsDTO(int validCredits) {
}
