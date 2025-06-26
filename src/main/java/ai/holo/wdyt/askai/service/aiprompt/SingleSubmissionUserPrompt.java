package ai.holo.wdyt.askai.service.aiprompt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SingleSubmissionUserPrompt extends AiPrompt {

    private final String style1;
    private final String style2;
    private final String style3;
    private final String occasion;
    private final String location;
    private final String date;
    private final String color1;
    private final String color2;
    private final String color3;

    public SingleSubmissionUserPrompt(Builder builder) {
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

        public SingleSubmissionUserPrompt build() {
            this.style1 = defaultIfBlank(this.style1, "minimalist");
            this.style2 = defaultIfBlank(this.style2, "elegant");
            this.style3 = defaultIfBlank(this.style3, "casual");

            this.occasion = defaultIfBlank(this.occasion, "general");
            this.location = defaultIfBlank(this.location, "global");
            this.date = defaultIfBlank(this.date, LocalDate.now().toString());

            this.color1 = defaultIfBlank(this.color1, "black");
            this.color2 = defaultIfBlank(this.color2, "white");
            this.color3 = defaultIfBlank(this.color3, "beige");

            return new SingleSubmissionUserPrompt(this);
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