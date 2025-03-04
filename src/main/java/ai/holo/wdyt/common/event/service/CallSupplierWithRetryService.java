package ai.holo.wdyt.common.event.service;

import ai.holo.wdyt.common.exception.InvalidImageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@Slf4j
public class CallSupplierWithRetryService {

    private static final int MAX_RETRY_COUNT = 3;

    public String executeWithRetries(Supplier<String> gptResponseSupplier) {
        int retries = MAX_RETRY_COUNT;
        for (int i = 0; i < retries; i++) {
            String gptResponse = null;
            try {
                return gptResponseSupplier.get();
            } catch (RuntimeException e) {
                if (i == retries - 1) { // If it's the last attempt, rethrow the exception
                    log.error("Failed to get response from AI service. Response: {}", gptResponse);
                    throw new InvalidImageException();
                }
                // Log the retry attempt
                log.warn("Retrying sendPromptWithImage due to failure: " + e.getMessage());

                // Sleep for 500ms before retrying
                sleep500Millis();
            }
        }
        throw new IllegalStateException("Unexpected error in retry logic"); // Should never reach here
    }

    private void sleep500Millis() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new RuntimeException("Retry was interrupted", interruptedException);
        }
    }
}
