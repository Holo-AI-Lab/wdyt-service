package ai.holo.wdyt.config.secret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration
public class SecretsManagerConfig {
    private final String region;
    private final String awsProfile;
    private final String secretName;

    public SecretsManagerConfig(@Value("${aws.region}") String region,
                                @Value("${aws.profile}") String awsProfile,
                                @Value("${aws.secret-name}") String secretName) {
        this.region = region;
        this.awsProfile = awsProfile;
        this.secretName = secretName;
    }

    @Bean
    public String fetchSecrets() {
        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(ProfileCredentialsProvider.create(awsProfile))
                .build()) {

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            GetSecretValueResponse response = client.getSecretValue(request);

            return response.secretString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve secret from AWS Secrets Manager", e);
        }
    }
}
