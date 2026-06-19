package ai.holo.wdyt.location.service;

import ai.holo.wdyt.location.model.GeoLocationDto;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.location.model.WeatherDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Service
public class IpGeoLocationService {

    private final WeatherService weatherService;

    public IpGeoLocationService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public LocationAndWeatherDto getLocationAndWeatherByIp(String ip) {
        GeoLocationDto location = getLocationByIp(ip);
        if (location.isUnknown()) {
            return new LocationAndWeatherDto(location, null);
        }

        WeatherDto weather = weatherService.getWeatherInformation(location)
                .map(WeatherService::toWeatherDto)
                .orElse(null);
        return new LocationAndWeatherDto(location, weather);
    }

    public GeoLocationDto getLocationByIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return createUnknownLocation();
        }
        try {
            WebClient webClient = WebClient.create("https://api.ipbase.com/json");

            GeoLocationResponse response = webClient.get()
                    .uri("/{ip}", ip)
                    .accept(MediaType.ALL)
                    .retrieve()
                    .bodyToMono(GeoLocationResponse.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            return response != null ? new GeoLocationDto(response.city, response.country, response.latitude, response.longitude) : createUnknownLocation();
        } catch (Exception e) {
            return createUnknownLocation();
        }
    }

    private GeoLocationDto createUnknownLocation() {
        return new GeoLocationDto("Unknown", "Unknown", "Unknown", "Unknown");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoLocationResponse(String city, @JsonProperty("country_name") String country, String latitude, String longitude) {
    }
}
