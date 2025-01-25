package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.util.List;

public record AiFeedbackSubmissionDto(Long aiFeedbackId,
                                      Long userId,
                                      String clientIpAddress,
                                      @NotNull ZonedDateTime clientTime,
                                      List<String> occasions,
                                      boolean bgExtracted,
                                      LocationAndWeatherDto locationAndWeather) {

}