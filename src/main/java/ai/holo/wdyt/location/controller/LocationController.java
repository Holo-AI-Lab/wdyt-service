package ai.holo.wdyt.location.controller;


import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.location.service.IpGeoLocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final IpGeoLocationService ipGeoLocationService;

    public LocationController(IpGeoLocationService ipGeoLocationService) {
        this.ipGeoLocationService = ipGeoLocationService;
    }

    @GetMapping("/location-and-weather/{ip}")
    public LocationAndWeatherDto getLocationAndWeather(@PathVariable String ip) {
        return ipGeoLocationService.getLocationAndWeatherByIp(ip);
    }
}
