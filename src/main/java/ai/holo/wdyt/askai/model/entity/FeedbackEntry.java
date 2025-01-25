package ai.holo.wdyt.askai.model.entity;

import ai.holo.wdyt.location.model.LocationAndWeatherDto;

import java.time.LocalDateTime;

public record FeedbackEntry(String id, Long userId, Long promptId, String response,
                            Boolean likeAiResponse,
                            LocationAndWeatherDto locationAndWeather, LocalDateTime createdAt) {
}
