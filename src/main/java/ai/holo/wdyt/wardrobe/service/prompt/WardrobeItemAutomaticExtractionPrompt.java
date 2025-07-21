package ai.holo.wdyt.wardrobe.service.prompt;

public class WardrobeItemAutomaticExtractionPrompt {

    public String getSystemPrompt() {
        return """
                Based on the pictures I uploaded, identify all the clothes and accessories worn by the people in the 
                pictures and return the information in the following strict JSON array format:
                
                [
                  {
                    "name": "The name of the current item, need to say the color + type (length no more than 30 characters)",
                    "content": "Stable diffusion prompt words for the current item. Please comprehensively and thoroughly list all details, colors, stripe styles and colors, patterns and all other visual characteristics as much as possible. Do not include any descriptions related to humans, usage scenarios, or other items. The description must exclusively focus on the current item without referencing any other objects. For example, if the current item is a top, only describe the top. Sets (including bikini sets, suits, etc.) must be split into individual pieces, each described separately, clearly indicating whether the current item is a top or pants.",
                    "label": "must be one of: tops, dresses, jumpsuits, bottoms, outerwear, footwear, accessories, swimwear",
                    "subLabel": "The detailed sub-category of the current item (no more than 15 characters)",
                    "colors": [multiple colors are possible in this format - {name: "The name of color of the current item. (no more than 15 characters in length)", code: "Color code, for example: #000000"}],
                    "season": "The applicable season of the current item，Spring, Summer, Autumn, Winter, All",
                    "colorStripesIntersecting": "Is it a striped style with multiple colors interlaced: true or false"
                  }
                  // ... more items
                ]
                
                Strict Guidelines:
                - The output **must** be valid JSON (starting with `[` and ending with `]`) and parsable by a JSON parser.
                - The `label` field **must only** contain one of these 8 values: tops, dresses, jumpsuits, bottoms, outerwear, footwear, accessories, swimwear.
                - Each object represents a **single** clothing or accessory item — no grouping or combined entries.
                - If the clothing is part of a set (e.g., bikini, suit), split it into individual items (e.g., top and bottom).
                - The `content` field should only describe the current item, using comma-separated, lowercase, single-word visual traits.
                - Do **not** include any mention of people, body parts, usage scenarios, or other unrelated items.
                - Avoid extra commentary, explanations, or formatting — return **only** the JSON array.
                """;
    }
}
