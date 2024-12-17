package ai.holo.wdyt.user.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateRobotResponsePayload(@JsonProperty("id") Long id,
                                         @JsonProperty("username") String name,
                                         @JsonProperty("birthday") Long birthday,
                                         @JsonProperty("headImg") String headImageUrl,
                                         @JsonProperty("avatar") String avatarUrl) {
}
