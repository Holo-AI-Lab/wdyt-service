package ai.holo.wdyt.config.secret;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class WdytSecrets {
    @Value("#{secretsManagerConfig.fetchSecrets()}")
    private String secrets;

    @Bean
    public Map<String, String> secretProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(secrets, Map.class);
    }
}
