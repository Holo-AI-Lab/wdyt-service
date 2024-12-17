package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.entity.ChatGptPrompt;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.askai.repository.PromptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PromptService {
    private final PromptRepository promptRepository;
    private final Map<ImageType, ChatGptPrompt> promptCache = new ConcurrentHashMap<>();
    private volatile LocalDateTime lastRefreshTime;

    private static final Duration REFRESH_INTERVAL = Duration.ofMinutes(5);

    public PromptService(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    public ChatGptPrompt getPrompt(ImageType imageType) {
        // Check if a cache refresh is needed
        if (isCacheExpired()) {
            refreshPromptCache(); // Refresh the cache if more than 5 minutes have passed
        }

        // Return the prompt from the cache or null if not available
        return promptCache.get(imageType);
    }

    private boolean isCacheExpired() {
        return lastRefreshTime == null || Duration.between(lastRefreshTime, LocalDateTime.now()).compareTo(REFRESH_INTERVAL) > 0;
    }

    private synchronized void refreshPromptCache() {
        if (!isCacheExpired()) {
            return;
        }

        List<ChatGptPrompt> activePrompts = promptRepository.findAllByActiveTrue();
        promptCache.clear();
        activePrompts.forEach(prompt -> promptCache.put(prompt.getImageType(), prompt));
        lastRefreshTime = LocalDateTime.now();
        log.info("Prompt cache refreshed at: " + lastRefreshTime);
    }
}
