package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@Slf4j
public class BackgroundExtractionService {

    public static final String BACKGROUND_EXTRACTION_MODEL = "u2net";
    private final WebClient webClient;

    public BackgroundExtractionService(@Value("${integrations.background-extraction.url}") String backgroundExtractionApiUrl) {
        this.webClient = WebClient.builder().baseUrl(backgroundExtractionApiUrl).build();
    }

    public InputStream extractBackground(byte[] image) {
        ByteArrayResource byteArrayResource = new ByteArrayResource(image) {
            @Override
            public String getFilename() {
                return "image.png"; // Specify the filename for proper handling
            }
        };

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", byteArrayResource).contentType(MediaType.IMAGE_PNG);
        builder.part("model", BACKGROUND_EXTRACTION_MODEL).contentType(MediaType.TEXT_PLAIN);

        byte[] response = webClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(byte[].class).block();
        if (response == null) {
            log.error("Failed to extract background");
            throw new BadRequestException("Provided image is not appropriate for AI processing.");
        }
        return new ByteArrayInputStream(response);
    }
}
