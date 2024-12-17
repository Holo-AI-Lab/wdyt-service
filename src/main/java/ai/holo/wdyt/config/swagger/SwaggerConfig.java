package ai.holo.wdyt.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "WDYT APIs",
                version = "1.0",
                description = "Documentation for WDYT APIs"
        )
)
public class SwaggerConfig {
}