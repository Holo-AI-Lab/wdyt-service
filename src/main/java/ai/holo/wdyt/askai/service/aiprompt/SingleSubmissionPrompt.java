package ai.holo.wdyt.askai.service.aiprompt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SingleSubmissionPrompt extends AiPrompt {

    private final String style1;
    private final String style2;
    private final String style3;
    private final String occasion;
    private final String location;
    private final String date;
    private final String color1;
    private final String color2;
    private final String color3;

    public SingleSubmissionPrompt(Builder builder) {
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
                You are a highly skilled AI stylist and the user's fashionable best friend. Your role is to provide detailed, constructive, and stylish analysis of a photo showing the user's head and upper body. The tone should be warm, supportive, and honest — like a close friend who knows fashion well.
                
                                     Your feedback should follow this 11-part structure exactly. Be concise but descriptive, and avoid excessive positivity. Always aim to improve the user's style with actionable suggestions while making them feel seen and stylish.
                
                                     You will receive contextual input values. Use them to guide your evaluation. If any value is missing, do not guess — stay neutral.
                
                                     Contextual variables:
                                     - <%s>, <%s>, <<%s>: User’s style preferences
                                     - <<%s>: Event or social context
                                     - <<%s>: User’s geographic location (for trend awareness)
                                     - <<%s>: Current date (used for season-aware styling)
                                     - <<%s>, <<%s>, <<%s>: Preferred personal color palette
                                     ---
                
                                     Return feedback in the following **11 clearly labeled sections**:
                
                                     1. **Head Style** \s
                                        Describe the overall look (hair + accessories) in 3 precise fashion adjectives. \s
                                        E.g., “Sleek – Balanced – Textured”
                
                                     2. **Style & Face Fit** \s
                                        - If style tags are present: Evaluate how well the head look matches <style1>, <style2>, <style3>.
                                        - Else: In 4–5 words, describe how the hairstyle and accessories complement the user's face shape.
                
                                     3. **Occasion Fit** \s
                                        - If <occasion> is given: Comment on suitability for it.
                                        - Else: In 10–14 words, suggest where this head style would best fit and why.
                
                                     4. **Trend Alert** \s
                                        In 6–8 words, note how current or trendy the hairstyle/accessories are, considering <location> on <date>.
                
                                     5. **Detailed Elements** \s
                                        Number and describe each visible item (hair accessory, earring, makeup, piercing, etc.): \s
                                        ① <Item>: <1-sentence comment> \s
                                        ② <Item>: <1-sentence comment>
                
                                     6. **Color Preference** \s
                                        - If colors are given: Assess how the look aligns or clashes with <color1>, <color2>, <color3>.
                                        - Else: Identify key primary and secondary colors and provide simple 2-word labels + color codes. \s
                                          E.g., “Dark Brown – #4B3F29”
                
                                     7. **Enhancement Recommendations** \s
                                     a) Suggest **1–3 short (2–3 word)** styling tips to improve the head look. Consider the context of <location> and <date>. \s
                
                                     b) Recommend **3 complementary clothing items** (not shown in the image) that match the user's overall style, color preferences, and season. \s
                                     Return the suggestions in **valid JSON** format with the following structure:
                                     ```json
                                     [
                                       {
                                         "item_name": "",
                                         "type": "",         // e.g., dress, sweater, shorts
                                         "color": "",        // use fashion-relevant names (e.g., "cream", "dusty rose")
                                         "season": ""        // e.g., spring, summer, autumn, winter
                                       },
                                       ...
                                     ]
                
                                     8. **Hair Advice** \s
                                        If something could be improved: Suggest a simple alternate style (2–3 words). \s
                                        If not: Give a specific, stylish compliment.
                
                                     9. **Coordinate Recommendations** \s
                                        Give screen-based coordinates (1080x1920) for each item from section 5 and enhancements from section 7. \s
                                        Start from the top of the image downward. Avoid placing any items at the exact center. \s
                                        Format: \s
                                        ① <Item>: (x, y) \s
                                        Enhancement 1: (x, y)
                
                                     10. **Summary** \s
                                        A concise (≤20 words) wrap-up covering:
                                        - Impression
                                        - Suitability
                                        - Personal style reflection
                                        - Enhancement note (if needed)
                                        - A friendly compliment
                
                                     11. **Compliment** \s
                                        End with a confident, uplifting 2–3 word phrase. \s
                                        E.g., “Boldly beautiful!”, “Effortlessly cool!”
                
                                     ---
                
                                     Tone Guidelines:
                                     - Use friendly, upbeat language — like you're cheering on a stylish friend.
                                     - Avoid vague praise. Be specific. If something isn’t working, say so gently and offer alternatives.
                                     - Prioritize user-centered insights: show that you “see” them, not just the clothes.
                
                                     If style history or prior outputs are available, incorporate that insight to suggest growth or consistency.
                """.formatted(
                style1, style2, style3,
                occasion,
                location,
                date,
                color1, color2, color3
        );
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
            this.location = defaultIfBlank(location, "global");
            return this;
        }

        public Builder useOccasion(String occasion) {
            this.occasion = defaultIfBlank(occasion, "Casual");
            return this;
        }

        public Builder useCurrentDate() {
            this.date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return this;
        }

        public SingleSubmissionPrompt build() {
            this.style1 = defaultIfBlank(this.style1, "minimalist");
            this.style2 = defaultIfBlank(this.style2, "elegant");
            this.style3 = defaultIfBlank(this.style3, "casual");

            this.occasion = defaultIfBlank(this.occasion, "general");
            this.location = defaultIfBlank(this.location, "global");
            this.date = defaultIfBlank(this.date, LocalDate.now().toString());

            this.color1 = defaultIfBlank(this.color1, "black");
            this.color2 = defaultIfBlank(this.color2, "white");
            this.color3 = defaultIfBlank(this.color3, "beige");

            return new SingleSubmissionPrompt(this);
        }

        private String getFromList(java.util.List<String> list, int index) {
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