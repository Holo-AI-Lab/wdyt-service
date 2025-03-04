package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.entity.ChatGptPrompt;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.askai.model.entity.SubmissionType;
import ai.holo.wdyt.askai.repository.PromptRepository;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.location.model.WeatherDto;
import ai.holo.wdyt.user.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PromptService {
    public static final String BODY_JSON_FORMATTING_PROMPT = "Tag styles, occasions and colors from the output and map to the tags field in the following json structure. We would like the response in this format: {\"outfit_style\":\"string\",\"style_match\":\"string\",\"occasion_fit\":\"string\",\"trend_alert\":\"string\",\"outfit_details\":[{\"item\":\"string\",\"color\":\"string\",\"description\":\"string\"}],\"color_preference\":{\"primary\":\"string\",\"secondary\":\"string\"},\"enhancement_recommendations\":[\"string\"],\"hair_advice\":\"string\",\"coordinate_recommendations\":{\"outfit\":[{\"x\":\"int\",\"y\":\"int\"}],\"enhancements\":[{\"x\":\"int\",\"y\":\"int\"}]},\"summary\":\"string\",\"compliment\":\"string\",\"tags\":[style:[\"string\"],occasion:[\"string\"],color:[\"string\"]]}";
    public static final String HEAD_JSON_FORMATTING_PROMPT = "Tag head styles, occasion fits and colors of any wearings from the output and map to the tags field in the following json structure. We would like the response in this format: {\"head_style\":\"string\",\"style_face_fit\":\"string\",\"occasion_fit\":\"string\",\"trend_alert\":\"string\",\"detailed_elements\":[{\"item\":\"string\",\"description\":\"string\",\"color\":\"string\"}],\"color_preference\":{\"primary\":\"string\",\"secondary\":\"string\"},\"enhancement_recommendations\":[\"string\"],\"hair_advice\":\"string\",\"coordinate_recommendations\":{\"elements\":[{\"x\":\"int\",\"y\":\"int\"}],\"enhancements\":[{\"x\":\"int\",\"y\":\"int\"}]},\"summary\":\"string\",\"compliment\":\"string\",\"tags\":[style:[\"string\"],occasion:[\"string\"],color:[\"string\"]]}";
    public static final String COMPARISON_JSON_FORMATTING_PROMPT = "Tag styles, occasions and colors from the winning outfit and map to the tags field in the following json structure. We would like the response in this json format: {\"outfitStyles\":\"string\",\"styleMatch\":{\"outfit1\":\"string\",\"outfit2\":\"string\"},\"occasionFit\":{\"outfit1\":\"string\",\"outfit2\":\"string\"},\"trendAlert\":{\"outfit1\":\"string\",\"outfit2\":\"string\"},\"colorPreference\":{\"outfit1\":\"string\",\"outfit2\":\"string\"},\"enhancementRecommendations\":{\"outfit1\":\"string\",\"outfit2\":\"string\"},\"hairAdvice\":{\"outfit1\":\"string\",\"outfit2\":\"string\"},\"winnerDetermination\":\"string\",\"summary\":\"string\",\"finalCompliment\":\"string\",\"winner\":\"number - 1 or 2\",\"tags\":{\"style\":[\"string\"],\"occasion\":[\"string\"],\"color\":[\"string\"]}}";

    private final PromptRepository promptRepository;
    private final Map<PromptKey, ChatGptPrompt> promptCache = new ConcurrentHashMap<>();
    private volatile LocalDateTime lastRefreshTime;

    private static final Duration REFRESH_INTERVAL = Duration.ofMinutes(5);

    public PromptService(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    public ChatGptPrompt getPrompt(ImageType imageType, SubmissionType submissionType) {
        // Check if a cache refresh is needed
        if (isCacheExpired()) {
            refreshPromptCache(); // Refresh the cache if more than 5 minutes have passed
        }

        // Return the prompt from the cache or null if not available
        PromptKey promptKey = new PromptKey(imageType, submissionType);
        return promptCache.get(promptKey);
    }

    public String getPromptText(ChatGptPrompt prompt, User aiUser, ZonedDateTime date, LocationAndWeatherDto locationAndWeather,
                                 List<String> occasions, SubmissionType submissionType, List<String> styles) {
        String formattedDate = date.toString();
        String promptText = prompt.getPrompt();
        String jsonFormattingSuffix = SubmissionType.COMPARE.equals(submissionType) ? COMPARISON_JSON_FORMATTING_PROMPT :
                ImageType.BODY.equals(prompt.getImageType()) ? BODY_JSON_FORMATTING_PROMPT : HEAD_JSON_FORMATTING_PROMPT;
        promptText = promptText + jsonFormattingSuffix;

        List<String> parameterOccasions = occasions != null ? occasions : List.of();
        String occasion = String.join(",", parameterOccasions);

        String locationRegex = "\\$\\{local}";
        String dateRegex = "\\$\\{date}";
        String weatherRegex = "\\$\\{weather}";
        String occasionRegex = "\\$\\{occasion}";
        String style1Regex = !styles.isEmpty() ? "\\$\\{style 1}" : null;
        String style2Regex = styles.size() >= 2 ? "\\$\\{style 2}" : null;
        String style3Regex = styles.size() >=3 ? "\\$\\{style 3}" : null;

        String location = locationAndWeather.location().getLocation();
        WeatherDto weather = locationAndWeather.weather();
        String weatherCondition = weather != null ? String.format("Temperature Celsius: %f, condition: %s", weather.tempC(),
                weather.condition()) : "Weather data not available";

        // Replace all occurrences of ${location} and ${date}
        promptText = promptText.replaceAll(locationRegex, location);
        promptText = promptText.replaceAll(dateRegex, formattedDate);
        promptText = promptText.replaceAll(weatherRegex, weatherCondition);
        promptText = promptText.replaceAll(occasionRegex, occasion);
        if (style1Regex != null) {
            promptText = promptText.replaceAll(style1Regex, styles.get(0));
        }
        if (style2Regex != null) {
            promptText = promptText.replaceAll(style2Regex, styles.get(1));
        }
        if (style3Regex != null) {
            promptText = promptText.replaceAll(style3Regex, styles.get(2));
        }
        return promptText.replaceAll(dateRegex, formattedDate);

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
        activePrompts.forEach(prompt -> {
            PromptKey key = new PromptKey(prompt.getImageType(), prompt.getSubmissionType());
            promptCache.put(key, prompt);
        });
        lastRefreshTime = LocalDateTime.now();
        log.info("Prompt cache refreshed at: " + lastRefreshTime);
    }

    record PromptKey(ImageType imageType, SubmissionType submissionType) {

    }
}

