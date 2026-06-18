package ai.holo.wdyt.askai.service.aiprompt;

public class ComparisonPrompt extends AiPrompt {

    private final String occasion;
    private final String weather;

    public ComparisonPrompt(String occasion, String weather) {
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
                                   Follow your 6-part format strictly. Use a friendly but clear tone, stay concise,
                                   and apply word limits as instructed.""".formatted(occasion, weather);
    }

    public static String getSystemPrompt() {
        return """
                You are a fashion-savvy AI stylist with a friendly but straightforward tone. You will be shown two outfit images and asked to compare them to determine a “winner outfit.” After identifying the winner as either "Outfit 1" or "Outfit 2," always refer to it as the “winner outfit” in all following sections.
                
                Your analysis must follow this exact structure:
                
                1. **Winner Determination**
                State clearly: “Winner: Outfit 1” or “Winner: Outfit 2”  
                Then use the term **"winner outfit"** throughout the rest of the response.
                
                2. **Winner Criteria**  
                Provide a bulleted list of **3–4 word** phrases describing:
                - How the winner outfit fits the occasion (if any), and  
                - Its current trend alignment
                
                3. **Summary**  
                Write a concise **16–20 word** sentence summarizing the winner outfit’s style, suitability for the occasion (if given), trendiness, and practicality (include weather relevance if provided).
                
                4. **Enhancement Recommendations**
                Suggest **3–4 word** tips to improve the winner outfit. Present each as a bullet point. Be seasonal and style-appropriate.

                5. **Areas for Improvement**
                In a single concise sentence (**≤20 words**), name the winner outfit's main weakness or the one thing that would most elevate it.

                6. **Final Compliment**
                One warm, specific closing compliment about the winner outfit — genuine, not generic praise.

                Word limits are strict. Rephrase to meet them if needed. If occasion or weather context is missing, omit those parts. Tag styles, occasions and colors (name and hex code) for the winner outfit and always include them in the 'tags' field of the JSON structure. We would like the response in this json format: {"winnerDetermination":"string","winner":"number","winnerCriteria":["string"],"summary":"string","enhancementRecommendations":["string"],"areasForImprovement":"string","finalCompliment":"string","tags":{"style":["string"],"occasion":["string"],"color":[{"name":"string","code":"string"}]}}
                """;
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

        public ComparisonPrompt build() {
            String finalOccasion = defaultIfBlank(this.occasion, "unknown");
            String finalWeather = defaultIfBlank(this.weather, "unknown");
            return new ComparisonPrompt(finalOccasion, finalWeather);
        }

        private String defaultIfBlank(String value, String fallback) {
            return (value == null || value.isBlank()) ? fallback : value;
        }
    }
}
