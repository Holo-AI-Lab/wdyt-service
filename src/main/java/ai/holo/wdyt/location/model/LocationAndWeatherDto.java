package ai.holo.wdyt.location.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public record LocationAndWeatherDto(GeoLocationDto location, WeatherDto weather) {
}
