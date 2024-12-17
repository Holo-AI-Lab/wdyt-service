package ai.holo.wdyt.askai.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ChatGptService {
    private final WebClient webClient;

    public ChatGptService(Map<String, String> secretProperties) {
        this.webClient = WebClient.builder().baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", String.format("Bearer %s", secretProperties.get("chatGptApiKey"))).build();
    }

    public String sendPromptWithImage(String imageUrl, String promptText) {
        List<Message> messages = List.of(new Message("user", List.of(new MessageContent("text", promptText, null),
                new MessageContent("image_url", null, new ImageAttachment(imageUrl)))));

        ChatGPTRequest request = new ChatGPTRequest("gpt-4o-mini", messages);

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> handleError(clientResponse, "Client error"))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> handleError(clientResponse, "Server error"))
                .bodyToMono(String.class)
                .block();

    }

    private static Mono<? extends Throwable> handleError(ClientResponse clientResponse, String errorType) {
        // Log the error details
        return clientResponse.bodyToMono(String.class)
                .doOnTerminate(() -> {
                    log.error("{} occurred: {}", errorType, clientResponse.statusCode());
                })
                .doOnNext(responseBody -> log.error("Response Body: {}", responseBody))
                .then(Mono.error(new RuntimeException(
                        String.format("%s: %s ", errorType, clientResponse.statusCode())
                )));
    }

    public record ChatGPTRequest(String model, List<Message> messages) {
    }

    public record Message(String role, List<MessageContent> content) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MessageContent(String type, String text, ImageAttachment image_url) {
    }

    public record ImageAttachment(String url) {
    }
}
