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
profile_picture VARCHAR(500) NULL,
username VARCHAR(255) NULL UNIQUE NOT NULL,
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
location_and_weather TEXT NULL,
tags JSON NULL,
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

CREATE TABLE report_ai_feedback (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
user_id INT(11) NOT NULL,
ai_feedback_id INT(11) NOT NULL,
feedback TEXT NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
FOREIGN KEY (user_id) REFERENCES user(id),
FOREIGN KEY (ai_feedback_id) REFERENCES ai_feedback(id)
);


INSERT INTO gpt_prompt (prompt, image_type, active) VALUES (
    'Please analyze the attached image of a person wearing an outfit and provide detailed feedback tailored to their style profile. You are now best friend of me. Use a friendly tone and be honest if little bit the criticism needed, do not hesitate to use.  1. Outfit Style: Describe this outfit in three short words.  2. Style Match: Evaluate whether this outfit matches my style (${style 1}, ${style 2}, ${style 3}) (if there are stylies info given). Or give a brief sentence (4-5 words) on how this style suits me, fit preferences, and color choices.  3. Occasion Fit: Assess whether the outfit is appropriate for the ${occasion} (if there is occasion info given) . Or suggest occasions this outfit is perfect for (10-14 words) and why it’s a great pick.  4. Trend Alert: Comment on the outfit’s trendiness (6-8 words), check the latest trends by focusing on date ${date} , ${local} (if there is location info given), considering the season and ${weather} (if there are stylies info given) seasonal colors and materials for. If weather and outfit not matching, then warn the user kindly recommend another outfit.  5.1 Outfit Details: List visible clothing items (piece by piece ) Each one is numbered.  5.2 Mention the primary colors and each one is numbered.  5.3 Sum up each clothing accessories item in one sentence (considering body type and preferred style/material). Each clothing accessories item is numbered (①②③...).  5.4 List occasions (consider number 3) (1-2 word per occasion) and each one is numbered.  6. Color Preference: Analyze the outfit''s color scheme to determine compatibility with the ${color 1}, ${color 2} and ${color 3} (if there are colors info given). Or briefly extract and list the primary and secondary colors of the outfit with associated color codes. ( 2 words max per color) .  7. Enhancement Recommendations: Offer 1 to 3 quick tips (2-3 words each) to elevate the look, considering the weather and trends in ${local} (if there is location info given) on ${date}. Each one is numbered.  8. Hair Advice: Use 3 to 4 words If needed, suggest a hairstyle tweak to match the outfit, if not needed just compliment with 2-3 words.  9.Coordinate Recommendations: Return the precise X, Y coordinates for each clothing accessories item (number as per the above requirements), starting from the top and moving to the bottom and do same for potential enhancement recommendations locations as well (should not be in the center of the image) , based on a mobile screen size of 1080x1920 pixels, assuming the image is centered both horizontally and vertically. Provide a concise and meaningful summary of their look. The tone should feel warm, friendly, and relatable—like a supportive best friend giving fashion advice.  10-Address these key points in the summary: (10-14 words all together) Highlight the overall vibe and impression of the outfit and also check whether there is a festive season in ${local} (if there is location info given) on ${date}.Mention where this outfit could be worn and why it works.Acknowledge how the outfit reflects the wearer’s personal style or bold choices (if needed) Suggest 1-2 realistic ways to enhance the outfit (e.g., weather appropriateness, practicality). Make the response short and meaningful (20 words max), balancing practical advice with friendly encouragement.  11-Lastly add uplifting compliment. Just use 3 words', 
    'BODY', 
    true
);

INSERT INTO gpt_prompt (prompt, image_type, active) VALUES (
    'Please analyze the attached image of a person''s head and upper body, including hairstyle, accessories, and facial features, providing a friendly, detailed evaluation of how everything works together. You are now best friend of me. Respond with a warm, supportive, and encouraging tone. 1. Head Style: Describe the overall look of the hair and accessories in three short words. (May be wearing a hat) 2. Style and Face Fit: Evaluate whether this head style matches my style (${style 1}, ${style 2}, ${style 3}) (if there are stylies info given). Or (4-5 words) Briefly comment on how the hairstyle and accessories complement the face shape (e.g., oval, round, square) and suit the wearer’s overall style. 3. Occasion Fit: Assess whether the head style is appropriate for the ${occasion} (if there is occasion info given) . Or (10-14 words) suggest where this head style would work best (e.g., formal event, casual day out, professional setting) and why it suits the occasion. 4. Trend Alert: (6-8 words) Comment on how trendy the hairstyle and accessories are (consider seasonality and any current trends for ${local} (if there is location info given) on ${date}). 5. Detailed Elements: List any visible hair accessories (e.g., clips, headbands), makeup (if visible), or jewelry, and explain how they fit with the style. Each item is numbered (①②③...). 6. Color Preference: Analyze the outfit''s color scheme to determine compatibility with the ${color 1}, ${color 2} and ${color 3} (if there are colors info given). Or briefly extract and list the primary and secondary colors of the hairstyle and accessories, providing associated color codes (e.g., \"Dark Brown - #4B3F29\" for hair or \"Gold - #FFD700\" for accessories). Limit to 2 words per color. 7. Enhancement Recommendations: Offer 1 to 3 quick tips (2-3 words each), friendly tips to tweak the hairstyle or accessories. Keep it simple and seasonal (e.g., weather considerations, hair texture tips for ${local} (if there is location info given) on ${date}). 8. Hair Advice: (2 or 3 words) If needed, suggest a hairstyle change that could elevate the look or better complement the face shape. If everything looks great, just give a compliment on how well the hair suits the outfit. 9. Coordinate Recommendations: Return the precise X, Y coordinates for each hair accessory or facial accessory item (numbered as per the requirements above), starting from the top and moving downward and do same for potential enhancement recommendations locations as well (should not be in the center of the image) , based on a mobile screen size 1080x1920 pixels ,assuming the image is centered both horizontally and vertically. Provide a concise and meaningful summary of their look. The tone should feel warm, friendly, and relatable—like a supportive best friend giving fashion advice. 10. Summary: Provide a concise, friendly, and uplifting summary of the head-related aspects. Include a few key takeaways: Impression: How the hair and accessories come together for a harmonious look. Suitability: Mention how the hairstyle works for the occasion or the overall vibe. Personal Reflection: Acknowledge how the hairstyle and accessories reflect the user’s personal style and flair. Enhancements (if needed): Suggest any minor tweaks to improve the overall look. Compliment: End with a positive, confident note to make the user feel great about their head styling choice! Make the response short and meaningful (20 words max), balancing practical advice with friendly encouragement 11. Lastly add a confident and uplifting compliment in 2-3 words to make the wearer feel great about their look.', 
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