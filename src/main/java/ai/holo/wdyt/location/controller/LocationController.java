package ai.holo.wdyt.location.controller;


import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.location.model.WeatherDto;
import ai.holo.wdyt.location.service.IpGeoLocationService;
import ai.holo.wdyt.location.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final IpGeoLocationService ipGeoLocationService;
    private final WeatherService weatherService;

    public LocationController(IpGeoLocationService ipGeoLocationService, WeatherService weatherService) {
        this.ipGeoLocationService = ipGeoLocationService;
        this.weatherService = weatherService;
    }

    @GetMapping("/location-and-weather/{ip}")
    public LocationAndWeatherDto getLocationAndWeather(@PathVariable String ip) {
        return ipGeoLocationService.getLocationAndWeatherByIp(ip);
    }

    @GetMapping("/weather")
    public ResponseEntity<WeatherDto> getWeather(@RequestParam String latitude,
                                                 @RequestParam String longitude) {
        return weatherService.getWeatherByCoordinates(latitude, longitude)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
