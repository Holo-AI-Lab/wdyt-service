package ai.holo.wdyt.common.chatgpt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ChatGptText2ImageService {
    private final WebClient webClient;

    public ChatGptText2ImageService(Map<String, String> secretProperties) {
        int maxSize = 20 * 1024 * 1024; // 20MB buffer

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxSize))
                .build();
        this.webClient = WebClient.builder().baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", String.format("Bearer %s", secretProperties.get("chatGptImageGenerationApiKey")))
                .exchangeStrategies(strategies)
                .build();
    }

    public byte[] generateImage(String prompt) {
        ChatGPTText2ImageRequest request = new ChatGPTText2ImageRequest("gpt-image-2", prompt, "low", "1024x1024", "png", "transparent");

        ChatGPTImageResponse response = webClient.post()
                .uri("/images/generations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> handleError(clientResponse, "Client error"))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> handleError(clientResponse, "Server error"))
                .bodyToMono(ChatGPTImageResponse.class)
                .block();
        if (response != null && response.data() != null && !response.data().isEmpty()) {
            String b64Json = response.data().get(0).b64Json();
            return Base64.getDecoder().decode(b64Json);
        }
        return null;
    }

    private static Mono<? extends Throwable> handleError(ClientResponse clientResponse, String errorType) {
        // Log the error details
        return clientResponse.bodyToMono(String.class)
                .doOnTerminate(() -> {
                    log.error("{} occurred: {}", errorType, clientResponse.statusCode());
                })
                .doOnNext(responseBody -> log.error("Response Body: {}", responseBody))
                .then(Mono.error(new RuntimeException(
                        String.format("Error on gpt service call - %s: %s ", errorType, clientResponse.statusCode())
                )));
    }

    public record ChatGPTText2ImageRequest(String model, String prompt, String quality, String size,
                                           @JsonProperty("output_format") String outputFormat, String background) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatGPTImageResponse(
            long created,
            List<ChatGPTImageData> data
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatGPTImageData(
            @JsonProperty("b64_json")
            String b64Json
    ) {}
}
