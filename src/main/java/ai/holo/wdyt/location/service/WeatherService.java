package ai.holo.wdyt.location.service;

import ai.holo.wdyt.location.model.GeoLocationDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Service
public class WeatherService {

    private final String weatherApiKey;

    public WeatherService(@Value("${integrations.weather.api-key}") String weatherApiKey) {
        this.weatherApiKey = weatherApiKey;
    }

    public Optional<WeatherResponse> getWeatherInformation(GeoLocationDto location) {
        if (location == null || location.isUnknown()) {
            return Optional.empty();
        }
        String url = String.format("http://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no",
                weatherApiKey, location.commaSeperatedLatLong());
        try {
            WebClient webClient = WebClient.create(url);

            return Optional.ofNullable(webClient.get()
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML)
                    .retrieve()
                    .bodyToMono(WeatherResponse.class)
                    .timeout(Duration.ofSeconds(20))
                    .block());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherResponse(WeatherCurrentResponse current) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherCurrentResponse(@JsonProperty("temp_c") double tempC, @JsonProperty("temp_f") double tempF, WeatherConditionResponse condition) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherConditionResponse(String text, String icon, String code) {
    }
}
