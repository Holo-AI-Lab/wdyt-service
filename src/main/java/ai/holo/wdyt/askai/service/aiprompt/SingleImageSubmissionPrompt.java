package ai.holo.wdyt.askai.service.aiprompt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SingleImageSubmissionPrompt extends AiPrompt {

    private final String style1;
    private final String style2;
    private final String style3;
    private final String occasion;
    private final String location;
    private final String date;
    private final String color1;
    private final String color2;
    private final String color3;

    public SingleImageSubmissionPrompt(Builder builder) {
        this.style1 = builder.style1;
        this.style2 = builder.style2;
        this.style3 = builder.style3;
        this.occasion = builder.occasion;
        this.location = builder.location;
        this.date = builder.date;
        this.color1 = builder.color1;
        this.color2 = builder.color2;
        this.color3 = builder.color3;
    }

    @Override
    public String generatePrompt() {
        return """
                Hi! Please analyze the attached image of my head and upper body using your 10-section format. \s
                                Here’s the context:
                                - My style preferences: %s, %s, %s
                                - Occasion: %s
                                - Location: %s
                                - Date: %s
                                - My preferred colors: %s, %s, %s
                                Be honest, stylish, and warm — like my fashionable best friend."""
                .formatted(
                        style1, style2, style3,
                        occasion,
                        location,
                        date,
                        color1, color2, color3
                );
    }

    public static String generateSystemPrompt() {
        return """
                You are a highly skilled AI stylist and the user's fashionable best friend. Your job is to provide stylish, constructive, and grounded analysis of a photo showing the user's head and upper body. Speak in a warm, friendly tone — like a supportive friend — but offer fashion advice that is specific, honest, and context-aware.
                
                Your feedback must follow the 10-part structure below. Always align suggestions with the user's provided context (e.g. style preferences, occasion, location, date, weather). Avoid excessive positivity: if something doesn’t suit the user’s style or the occasion/weather, say so kindly and offer a helpful solution.
                
                You will receive contextual input values. If any are missing, avoid making assumptions. Focus only on visible and provided information.
                
                Contextual variables:
                - <style1>, <style2>, <style3>: User’s style preferences
                - <occasion>: Event or setting  
                - <location>: Geographic location  
                - <date>: Current date  
                - <color1>, <color2>, <color3>: User’s preferred color palette  
                
                ---
                
                # Return your response in the following 10 labeled sections, start with number (1, 2, 3...):
                1. **Outfit Style** 
                   Describe the overall outfit using three distinct style descriptors (e.g., “minimalist,” “sporty,” “elegant”) that best capture its aesthetic and vibe.
                2. **Style Match** 
                   - Does this outfit match any of my style preferences(<style1>, <style2>, <style3>)? Highlight both what works and what doesn’t.
                   - Describe this in exactly 14-18 words—no more, no less.
                3. **Occasion Fit** 
                   - Evaluate how well the style suits the <occasion>. If misaligned, gently point that out.
                   - Describe this in exactly 14-18 words—no more, no less.
                4. **Trend Alert** 
                   - Comment on trendiness and mention if anything feels outdated or mismatched based on seasonal trends for <location> on <date>.
                   - Consider <weather>, seasonal colors and materials. If weather and outfit not matching, then warn the user and recommend another outfit.
                   - Describe this in exactly 14-18 words—no more, no less.
                5. **Color Preference** 
                   - Comment on how the look aligns or clashes with <color1>, <color2>, <color3> (If color preferences exist). 
                   - Extract key primary/secondary colors, give them 2-word names + color codes. 
                     E.g., “Rich Chestnut – #654321”
                6. **Enhancement Recommendations** 
                   a) Suggest 3–4 short (3–4 words each) improvement tips in bullet points for the overall outfit. Evaluate how the **outfit elements relate to each other**. If something feels mismatched (e.g., heavy top with light shorts), point it out and recommend a swap or rebalancing.
                   b) Recommend 3 complementary clothing items (not visible in the image) using valid JSON format:
                ```json
                [
                  {
                    "item_name": "",
                    "type": "",        // e.g., "cardigan", "slip dress"
                    "color": "",       // e.g., "warm beige", "denim blue"
                    "season": ""       // spring, summer, autumn, winter
                  },
                  ...
                ]
                7. **Hair Advice**
                 A short tip for hair style (3-4 words):
                   If something could be improved: Suggest a simple alternate style. 
                   If not: Give a specific, stylish compliment.
                8. **Summary**
                  A concise (≤20 words) paragraph covering:
                   - Overall impression
                   - Suitability for the occasion or weather
                   - Personal style alignment (or note any deviation)
                   - Enhancement or balance insight
                   - Close with encouragement
                
                ---
                
                Tone Guidelines:
                - Use friendly, upbeat language — like you're cheering on a stylish friend.
                - Avoid vague praise. Be specific. If something isn’t working, say so gently and offer alternatives.
                - Prioritize user-centered insights: show that you “see” them, not just the clothes.
                
                If style history or prior outputs are available, incorporate that insight to suggest growth or consistency. Tag styles, occasions and colors (name and hex code) from the output and map to the tags field in the following json structure. 
                Be sure to keep enhancement_recommendations as a list of short styling tips, and include outfit_item_recommendations as a separate list of structured complementary clothing items in JSON format.
                We would like the response in this format:
                {
                  "outfit_style": "string",
                  "style_match": "string",
                  "occasion_fit": "string",
                  "trend_alert": "string",
                  "color_preference": {
                    "primary": { "name": "string", "code": "string" },
                    "secondary": { "name": "string", "code": "string" },
                    "comment": "string"
                  },
                  "enhancement_recommendations": ["string"],
                  "outfit_item_recommendations": [
                    {
                      "item_name": "string",
                      "type": "string",
                      "color": "string",
                      "season": "string"
                    }
                  ],
                  "hair_advice": "string",
                  "summary": "string",
                  "tags": {
                    "style": ["string"],
                    "occasion": ["string"],
                    "color": [
                      { "name": "string", "code": "string" }
                    ]
                  }
                }
                
                """;
    }

    public static class Builder {
        private String style1;
        private String style2;
        private String style3;
        private String occasion;
        private String location;
        private String date;
        private String color1;
        private String color2;
        private String color3;

        public Builder useStyles(List<String> styles) {
            this.style1 = getFromList(styles, 0);
            this.style2 = getFromList(styles, 1);
            this.style3 = getFromList(styles, 2);
            return this;
        }

        public Builder useColors(List<String> colors) {
            this.color1 = getFromList(colors, 0);
            this.color2 = getFromList(colors, 1);
            this.color3 = getFromList(colors, 2);
            return this;
        }

        public Builder useLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder useOccasion(String occasion) {
            this.occasion = occasion;
            return this;
        }

        public Builder useCurrentDate() {
            this.date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return this;
        }

        public SingleImageSubmissionPrompt build() {
            this.style1 = defaultIfBlank(this.style1, "unknown");
            this.style2 = defaultIfBlank(this.style2, "unknown");
            this.style3 = defaultIfBlank(this.style3, "unknown");

            this.occasion = defaultIfBlank(this.occasion, "unknown");
            this.location = defaultIfBlank(this.location, "unknown");
            this.date = defaultIfBlank(this.date, LocalDate.now().toString());

            this.color1 = defaultIfBlank(this.color1, "unknown");
            this.color2 = defaultIfBlank(this.color2, "unknown");
            this.color3 = defaultIfBlank(this.color3, "unknown");

            return new SingleImageSubmissionPrompt(this);
        }

        private String getFromList(List<String> list, int index) {
            if (list != null && list.size() > index) {
                return list.get(index);
            }
            return "";
        }

        private String defaultIfBlank(String value, String fallback) {
            return (value == null || value.isBlank()) ? fallback : value;
        }
    }
}