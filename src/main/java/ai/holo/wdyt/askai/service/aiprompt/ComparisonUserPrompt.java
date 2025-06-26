package ai.holo.wdyt.askai.service.aiprompt;

public class ComparisonUserPrompt extends AiPrompt {

    private final String occasion;
    private final String weather;

    public ComparisonUserPrompt(String occasion, String weather) {
        this.occasion = occasion;
        this.weather = weather;
    }

    @Override
    public String generatePrompt() {
        return """
                Please analyze the two attached images and determine the winner outfit between them (Outfit 1 or Outfit 2). \s
                                   Here’s some additional context:
                                   - Occasion: %s \s
                                   - Weather: %s
                                   Follow your 4-part format strictly. Use a friendly but clear tone, stay concise, 
                                   and apply word limits as instructed.""".formatted(occasion, weather);
    }

    public static class Builder {
        private String occasion;
        private String weather;

        public Builder setOccasion(String occasion) {
            this.occasion = occasion;
            return this;
        }

        public Builder setWeather(String weather) {
            this.weather = weather;
            return this;
        }

        public ComparisonUserPrompt build() {
            return new ComparisonUserPrompt(occasion, weather);
        }
    }
}
