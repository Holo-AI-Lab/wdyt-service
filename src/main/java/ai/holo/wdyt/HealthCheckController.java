package ai.holo.wdyt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @GetMapping("/secured/health")
    public String securedHealthCheck() {
        return "SECURED OK";
    }
}