package ai.holo.wdyt.common.chatgpt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final String gptVersion;

    public ChatGptService(Map<String, String> secretProperties,
                          @Value("${chatgpt.version}") String gptVersion) {
        this.webClient = WebClient.builder().baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", String.format("Bearer %s", secretProperties.get("chatGptApiKey"))).build();
        this.gptVersion = gptVersion;
    }

    public String sendPrompt(List<Message> messages) {
        return sendPrompt(messages, null);
    }

    public String sendPrompt(List<Message> messages, Double temperature) {
        ChatGPTRequest request = new ChatGPTRequest(gptVersion, messages, temperature);

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> handleError(clientResponse, "Client error"))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> handleError(clientResponse, "Server error"))
                .bodyToMono(String.class)
                .block();
    }

    public String sendPromptWithImage(String imageUrl, String systemPrompt, String userPrompt) {
        return sendPromptWithImage(imageUrl, systemPrompt, userPrompt, null);
    }

    public String sendPromptWithImage(String imageUrl, String systemPrompt, String userPrompt, Double temperature) {
        List<Message> messages = List.of(
                new Message("system", List.of(new MessageContent("text", systemPrompt, null))),
                new Message("user", List.of(new MessageContent("text", userPrompt, null), new MessageContent("image_url", null, new ImageAttachment(imageUrl)))));

        return sendPrompt(messages, temperature);
    }

    public String sendPromptWith2Images(String imageUrl1, String imageUrl2, String promptText, String systemPrompt) {
        return sendPromptWith2Images(imageUrl1, imageUrl2, promptText, systemPrompt, null);
    }

    public String sendPromptWith2Images(String imageUrl1, String imageUrl2, String promptText, String systemPrompt, Double temperature) {
        List<Message> messages = List.of(
                new Message("system", List.of(new MessageContent("text", systemPrompt, null))),
                new Message("user", List.of(
                new MessageContent("text", promptText, null),
                new MessageContent("text", "Image URL for outfit 1 as follows:", null),
                new MessageContent("image_url", null, new ImageAttachment(imageUrl1)),
                new MessageContent("text", "Image URL for outfit 2 as follows:", null),
                new MessageContent("image_url", null, new ImageAttachment(imageUrl2))
        )));

        return sendPrompt(messages, temperature);
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ChatGPTRequest(String model, List<Message> messages, Double temperature) {
    }

    public record Message(String role, List<MessageContent> content) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MessageContent(String type, String text, ImageAttachment image_url) {
    }

    public record ImageAttachment(String url) {
    }
}
