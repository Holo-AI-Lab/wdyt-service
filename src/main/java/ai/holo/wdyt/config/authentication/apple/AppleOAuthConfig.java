package ai.holo.wdyt.config.authentication.apple;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AppleOAuthConfig {
    @Bean
    public AppleClientSecretGenerator appleClientSecretGenerator(
            Map<String, String> secretProperties) {
        return new AppleClientSecretGenerator(
                secretProperties.get("appleTeamId"),
                secretProperties.get("appleKeyId"),
                secretProperties.get("applePrivateKey"),
                secretProperties.get("appleClientId"),
                secretProperties.get("appleSubscriptionIssuerId"),
                secretProperties.get("appleSubscriptionKeyId"),
                secretProperties.get("appleSubscriptionKey"));
    }
}
