package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.util.List;

public record AiComparisonSubmissionDto(@NotNull Long feedback1,
                                        @NotNull Long feedback2,
                                        String clientIpAddress,
                                        @NotNull ZonedDateTime clientTime,
                                        List<String> occasions,
                                        LocationAndWeatherDto locationAndWeather) {

}