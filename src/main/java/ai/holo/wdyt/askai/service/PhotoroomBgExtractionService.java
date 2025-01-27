package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.common.exception.InvalidImageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.util.Map;

@Service
@Slf4j
public class PhotoroomBgExtractionService {

    private final WebClient webClient;

    public PhotoroomBgExtractionService(Map<String, String> secretProperties) {
        this.webClient = WebClient.builder()
                .baseUrl("https://sdk.photoroom.com/v1/segment")
                .defaultHeader("Accept", "image/png, application/json")
                .defaultHeader("Content-Type", "multipart/form-data")
                .defaultHeader("x-api-key", secretProperties.get("photoroomApiKey"))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    public ByteArrayInputStream extractBackground(byte[] image, String rawImagePath) {
        ByteArrayResource byteArrayResource = new ByteArrayResource(image) {
            @Override
            public String getFilename() {
                return "image_file"; // Provide a filename for the multipart data
            }
        };

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image_file", byteArrayResource)
                .contentType(MediaType.IMAGE_PNG);
        builder.part("crop", true);

        byte[] response = webClient.post()
                .uri("https://sdk.photoroom.com/v1/segment") // Adjust the URI if necessary
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(byte[].class)
                .doOnError(error -> {
                    throw new RuntimeException("Failed to extract background", error);
                })
                .block();

        if (response == null) {
            log.error("Failed to extract background for image {} with Photoroom.", rawImagePath);
            throw new InvalidImageException();
        }

        return new ByteArrayInputStream(response);
    }
}
