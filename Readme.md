# DB Scripts

``` 
CREATE DATABASE wdyt;

CREATE TABLE wdyt_robot (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(255) NOT NULL,
gender VARCHAR(255) NOT NULL DEFAULT 'FEMALE',
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
birthday DATE,
head_image_url VARCHAR(500) NULL,
avatar_url VARCHAR(500) NULL
);

CREATE TABLE user (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
robot_id INT(11) NULL,
email VARCHAR(255) UNIQUE NOT NULL,
name VARCHAR(255) NULL,
apple_id VARCHAR(255) UNIQUE NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
FOREIGN KEY (robot_id) REFERENCES wdyt_robot(id)
);

CREATE TABLE gpt_prompt (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
prompt TEXT NOT NULL,
image_type VARCHAR(255) NOT NULL DEFAULT 'BODY',
active boolean DEFAULT true
);


CREATE TABLE ai_feedback (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
user_id INT(11) NOT NULL,
prompt_id INT(11) NOT NULL,
response TEXT NOT NULL,
raw_image_path VARCHAR(500) NOT NULL,
image_type VARCHAR(255) NOT NULL DEFAULT 'OTHER',
extracted_image_path VARCHAR(500) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
like_style BOOLEAN DEFAULT false,
like_ai_response BOOLEAN NULL,
top_list_order INT(11) NULL,
standard_order INT(11) NULL,
location VARCHAR(500) NULL,
FOREIGN KEY (user_id) REFERENCES user(id),
FOREIGN KEY (prompt_id) REFERENCES gpt_prompt(id)
);

CREATE TABLE ai_feedback_order (
user_id INT(11) NOT NULL PRIMARY KEY,
last_order INT(11) NOT NULL,
FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE user_feedback (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
user_id INT(11) NOT NULL,
feedback TEXT NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
FOREIGN KEY (user_id) REFERENCES user(id)
);

INSERT INTO gpt_prompt (prompt, image_type, active) 
VALUES (
    'Please analyze the attached image of a person wearing an outfit and provide detailed feedback tailored to their style profile. You are now best friend of me. Use a friendly tone. 1. Outfit Style: Describe this outfit in three short words. 2. Style Match: Give a brief sentence (4-5 words) on how this style suits me, fit preferences, and color choices. 3. Occasion Fit: Suggest occasions this outfit is perfect for (10-14 words) and why it’s a great pick. 4. Trend Alert: Comment on the outfit’s trendiness (6-8 words), focusing on seasonal colors and materials for ${local} (if there is location info given), considering the season and date ${date}. 5. Outfit Details: List visible clothing items. Mention the primary colors. Sum up each clothing accessories item in one sentence (considering body type and preferred style/material). Each clothing accessories item is numbered (①②③...). 6. Color Preference: Briefly extract and list the primary and secondary colors of the outfit with associated color codes (2 words max per color). 7. Enhancement Recommendations: Offer 1 to 3 quick tips (2-3 words each) to elevate the look, considering the weather and trends in ${local} (if there is location info given) on ${date}. Each one is numbered. 8. Hair Advice: (2 or 3 words) If needed, suggest a hairstyle tweak to match the outfit. If everything’s perfect, just say something complimentary. 9. Coordinate Recommendations: Return the precise X, Y coordinates for each clothing accessories item (number as per the above requirements), starting from the top and moving to the bottom and do the same for potential enhancement recommendations locations as well (should not be in the center of the image), based on a mobile screen size of 1080x1920 pixels, assuming the image is centered both horizontally and vertically. Provide a concise and meaningful summary of their look. The tone should feel warm, friendly, and relatable—like a supportive best friend giving fashion advice. 10. Address these key points in the summary: Highlight the overall vibe and impression of the outfit and also check whether there is a festive season in ${local} (if there is location info given) on ${date}. Mention where this outfit could be worn and why it works. Acknowledge how the outfit reflects the wearer’s personal style or bold choices. Enhancements (if needed): Suggest 1-2 realistic ways to enhance the outfit (e.g., weather appropriateness, practicality). Make the response short and meaningful (20 words max), balancing practical advice with friendly encouragement. 11. Lastly, add a confident and uplifting compliment in 2-3 words to make the wearer feel great about their look.',
    'BODY',
    true
);

INSERT INTO gpt_prompt (prompt, image_type, active) 
VALUES (
    'Please analyze the attached image of a person''s head and upper body, including hairstyle, accessories, and facial features, and provide a friendly, detailed evaluation of how everything works together. You are now best friend of me. Use a warm, supportive, and encouraging tone. 1. Head Style: Describe the overall look of the hair and accessories in three short words. (May include hats or other headwear.) 2. Style and Face Fit: Provide a brief sentence (4-5 words) on how the hairstyle and accessories suit the face shape (e.g., oval, round, square) and complement the wearer''s style. 3. Occasion Fit: Suggest occasions this head style would work best for (10-14 words) and why it’s suitable. 4. Trend Alert: Comment on how trendy the hairstyle and accessories are (6-8 words), considering seasonal trends in ${local} on ${date}. 5. Detailed Elements: List visible hair accessories, makeup (if shown), and jewelry, explaining how each fits the overall style. Number each item (①②③...). 6. Color Preference: Briefly extract and list the primary and secondary colors of the hairstyle and accessories with associated color codes (e.g., "Dark Brown - #4B3F29" or "Gold - #FFD700"). Limit to 2 words per color. 7. Enhancement Recommendations: Offer 1 to 3 quick tips (2-3 words each) to tweak the hairstyle or accessories. Suggestions should be simple, friendly, and seasonal for ${local} on ${date}. Number the recommendations (①②③...). 8. Hair Advice: Provide a hairstyle tweak (2-3 words) if needed to elevate the look or better suit the face shape. If everything looks great, give a compliment on how well the hair suits the overall look. 9. Coordinate Recommendations: Return precise X, Y coordinates for each hair or facial accessory (as numbered above) and potential enhancement recommendations. Use a mobile screen size of 1080x1920 pixels, assuming the image is centered both horizontally and vertically. 10. Summary: Provide a concise, friendly, and uplifting summary of the head styling. Address these key points: Impression: How the hair and accessories create a cohesive and harmonious look. Suitability: Highlight how the hairstyle suits the occasion and overall vibe. Personal Reflection: Mention how the hairstyle and accessories reflect the wearer’s personal style or bold choices. Enhancements (if needed): Suggest 1-2 realistic tweaks to improve the overall look (e.g., weather appropriateness or practicality). Compliment: End with a confident, encouraging compliment to make the wearer feel great! Keep it short and meaningful (20 words max). 11. Compliment: Add a confident, uplifting compliment in 2-3 words to make the wearer feel amazing about their head styling.', 
    'HEAD_SHOT', 
    true
);



```

# Create Docker image and push to ECR
```
1-  ./gradlew build
2-  docker build --platform linux/amd64 -t wdyt-service .
3- aws ecr get-login-password --region us-east-1 --profile fr-cli | docker login --username AWS --password-stdin 071094189941.dkr.ecr.us-east-1.amazonaws.com
4- docker tag wdyt-service:latest 071094189941.dkr.ecr.us-east-1.amazonaws.com/wdyt:latest 
5- docker push 071094189941.dkr.ecr.us-east-1.amazonaws.com/wdyt:latest 

```