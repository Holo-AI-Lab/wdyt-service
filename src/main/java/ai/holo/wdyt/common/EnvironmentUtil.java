package ai.holo.wdyt.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentUtil {

    private final String profile;

    public EnvironmentUtil(@Value("${spring.profiles.active}") String profile) {
        this.profile = profile;
    }

    public boolean isDevelopment() {
        return profile.equals("dev");
    }
}
