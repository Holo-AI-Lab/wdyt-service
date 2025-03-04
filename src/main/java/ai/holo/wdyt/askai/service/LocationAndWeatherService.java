package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.common.exception.BadRequestException;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.location.service.IpGeoLocationService;
import org.springframework.stereotype.Service;

@Service
public class LocationAndWeatherService {
    private final IpGeoLocationService ipGeoLocationService;

    public LocationAndWeatherService(IpGeoLocationService ipGeoLocationService) {
        this.ipGeoLocationService = ipGeoLocationService;
    }

    public LocationAndWeatherDto getLocationAndWeather(LocationAndWeatherDto locationAndWeather, String clientIpAddress) {
        if(locationAndWeather == null) {
            if (clientIpAddress == null) {
                throw new BadRequestException("clientIpAddress is required when locationAndWeather is not provided");
            }
            // Get location and weather by IP
            locationAndWeather = ipGeoLocationService.getLocationAndWeatherByIp(clientIpAddress);
        }
        return locationAndWeather;
    }
}
