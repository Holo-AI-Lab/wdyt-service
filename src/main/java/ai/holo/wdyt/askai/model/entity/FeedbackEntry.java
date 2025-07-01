package ai.holo.wdyt.askai.model.entity;

import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FeedbackEntry(String id, Long userId, String response,
                            LocationAndWeatherDto locationAndWeather, LocalDateTime createdAt) {
}
