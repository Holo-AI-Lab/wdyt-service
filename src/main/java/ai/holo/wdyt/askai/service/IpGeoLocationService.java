package ai.holo.wdyt.askai.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class IpGeoLocationService {

    public String getLocationByIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "Unknown";
        }
        try {
            WebClient webClient = WebClient.create("https://freegeoip.app/json");

            GeoLocation response = webClient.get()
                    .uri("/{ip}", ip)
                    .retrieve()
                    .bodyToMono(GeoLocation.class).block();

            return response != null ? String.format("%s/%s", response.city(), response.country()) : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoLocation(String city, String country) {
    }
}
