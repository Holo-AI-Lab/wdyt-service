package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.entity.ImageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class ImageClassificationService {
    private final WebClient webClient;

    public ImageClassificationService(@Value("${integrations.image-classification.url}") String imageClassificationApiUrl) {
        this.webClient = WebClient.builder().baseUrl(imageClassificationApiUrl).build();
    }

    public ImageType classifyImage(byte[] image) {
        ByteArrayResource byteArrayResource = new ByteArrayResource(image) {
            @Override
            public String getFilename() {
                return "image.png"; // Specify the filename for proper handling
            }
        };

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", byteArrayResource).contentType(MediaType.IMAGE_PNG);

        ImageClassificationResponse response = webClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(ImageClassificationResponse.class).block();
        if (response == null) {
            log.error("Image classification failed");
            throw  new RuntimeException("Failed to classify image");
        }
        return getImageType(response);
    }

    private ImageType getImageType(ImageClassificationResponse response) {
        return switch (response.res()) {
            case 1 -> ImageType.BODY;
            default -> ImageType.OTHER;
        };
    }

    public record ImageClassificationResponse(int res) {
    }
}
