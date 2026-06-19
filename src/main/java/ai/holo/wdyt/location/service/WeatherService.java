package ai.holo.wdyt.location.service;

import ai.holo.wdyt.location.model.GeoLocationDto;
import ai.holo.wdyt.location.model.WeatherDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WeatherService {

    private static final String WEATHER_URL =
            "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric";
    private static final String ICON_URL = "http://openweathermap.org/img/wn/%s@2x.png";

    private final String weatherApiKey;

    public WeatherService(Map<String, String> secretProperties) {
        this.weatherApiKey = secretProperties.get("openWeatherApiKey");
    }

    public Optional<WeatherDto> getWeatherByCoordinates(String latitude, String longitude) {
        if (latitude == null || latitude.isBlank() || longitude == null || longitude.isBlank()) {
            return Optional.empty();
        }
        GeoLocationDto location = new GeoLocationDto(null, null, latitude, longitude);
        return getWeatherInformation(location).map(WeatherService::toWeatherDto);
    }

    public static WeatherDto toWeatherDto(WeatherResponse response) {
        double tempC = response.main().temp();
        double tempF = tempC * 9 / 5 + 32;
        WeatherCondition condition = response.firstCondition();
        String conditionText = condition != null ? condition.main() : null;
        String iconUrl = condition != null && condition.icon() != null
                ? String.format(ICON_URL, condition.icon()) : null;
        return new WeatherDto(tempC, tempF, conditionText, iconUrl);
    }

    public Optional<WeatherResponse> getWeatherInformation(GeoLocationDto location) {
        if (location == null || location.isUnknown()) {
            return Optional.empty();
        }
        String url = String.format(WEATHER_URL, location.latitude(), location.longitude(), weatherApiKey);
        try {
            WebClient webClient = WebClient.create(url);

            return Optional.ofNullable(webClient.get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(WeatherResponse.class)
                    .timeout(Duration.ofSeconds(20))
                    .block());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherResponse(List<WeatherCondition> weather, Main main) {

        public WeatherCondition firstCondition() {
            return weather != null && !weather.isEmpty() ? weather.get(0) : null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(double temp) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherCondition(String main, String description, String icon) {
    }
}
