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
username VARCHAR(255) UNIQUE NOT NULL,
apple_id VARCHAR(255) UNIQUE NOT NULL,
is_style_adapted BOOLEAN DEFAULT true,
user_selected_style VARCHAR(1000) NULL,
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
raw_image_path VARCHAR(500) NOT NULL,
image_type VARCHAR(255) NOT NULL DEFAULT 'OTHER',
extracted_image_path VARCHAR(500) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
like_style BOOLEAN DEFAULT false,
top_list_order INT(11) NULL,
standard_order INT(11) NULL,
tags JSON NULL,
feedback_entries JSON NULL,
FOREIGN KEY (user_id) REFERENCES user(id)
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

CREATE TABLE occasion (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(500) NOT NULL
);

CREATE TABLE style (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(500) NOT NULL
);

CREATE TABLE contact_us (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(255) NOT NULL,
email VARCHAR(255) NOT NULL,
subject VARCHAR(255) NOT NULL,
message TEXT NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE user_friend_request (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
user_id INT(11) NOT NULL,
friend_id INT(11) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
FOREIGN KEY (user_id) REFERENCES user(id),
FOREIGN KEY (friend_id) REFERENCES user(id)
);

CREATE TABLE user_friend (
id INT(11) AUTO_INCREMENT PRIMARY KEY,
user_id INT(11) NOT NULL,
friend_id INT(11) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
FOREIGN KEY (user_id) REFERENCES user(id),
FOREIGN KEY (friend_id) REFERENCES user(id)
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

INSERT INTO occasion (name) VALUES ('Outfit Occasions'), ('Casual Outing'), ('Coffee Date'), ('Formal Dinner'), ('Birthday Party'), ('Wedding'), ('Job Interview'), ('Picnic'), ('Movie Night'), ('Beach Day'), ('Hiking Trip'), ('Gala Event'), ('Music Concert'), ('Family Gathering'), ('Night Out with Friends'), ('Shopping Trip'), ('Brunch with Friends'), ('Corporate Meeting'), ('Date Night'), ('Dinner with Family'), ('BBQ Party'), ('Holiday Celebration'), ('Graduation Ceremony'), ('Bridal Shower'), ('Baby Shower'), ('Anniversary Dinner'), ('Casual Friday at Work'), ('Charity Event'), ('Beach Vacation'), ('Road Trip'), ('Networking Event'), ('Casual Office Day'), ('Sports Game'), ('Gym Session'), ('Pool Party'), ('Backyard BBQ'), ('Wedding Reception'), ('Art Gallery Opening'), ('Outdoor Concert'), ('Winter Wonderland Trip'), ('Summer Festival'), ('Dinner Date'), ('Romantic Getaway'), ('Casual Meeting'), ('Weekend Getaway'), ('Craft Fair'), ('Farmers Market Visit'), ('Family Reunion'), ('Amusement Park Day'), ('Zoo Visit'), ('Theme Park Visit'), ('Winter Ski Trip'), ('Poolside Brunch'), ('Museum Visit'), ('Sunset Walk'), ('Dinner with Friends'), ('Holiday Party'), ('Cocktail Party'), ('Wine Tasting'), ('Book Club Meeting'), ('Coffee with Friends'), ('Work Conference'), ('Spa Day'), ('Fashion Show'), ('Film Premiere'), ('Dance Party'), ('Picnic in the Park'), ('Wedding Anniversary Celebration'), ('Late Night Walk'), ('Early Morning Jog'), ('Garden Party'), ('Formal Lunch'), ('Board Game Night'), ('Farmers Market'), ('Beach Bonfire'), ('Vacation at a Resort'), ('Boat Trip'), ('Ski Resort Day'), ('Outdoor Adventure'), ('Family Picnic'), ('Art Class'), ('Cooking Class'), ('Barbecue Gathering'), ('Ice Skating'), ('Rock Climbing'), ('Tea Party'), ('Bowling Night'), ('Shopping Mall Visit'), ('Hangout with Siblings'), ('Sunday Brunch'), ('Get-Together at a Friend''s House'), ('Sports Practice'), ('Birthday Celebration'), ('Staycation'), ('Thanksgiving Dinner'), ('Christmas Gathering'), ('New Year''s Eve Party'), ('First Date'), ('Weekend Brunch'), ('Dinner at a Fancy Restaurant'), ('Vacation Excursion');
INSERT INTO style (name) VALUES ('Outfit Styles'), ('Casual'), ('Trendy'), ('Preppy'), ('Boho'), ('Vintage'), ('Minimalist'), ('Streetwear'), ('Athleisure'), ('Chic'), ('Classic'), ('Grunge'), ('Elegant'), ('Sporty'), ('Goth'), ('Punk'), ('Retro'), ('Artistic'), ('Eclectic'), ('Feminine'), ('Tomboy'), ('Androgynous'), ('Sophisticated'), ('Casual Elegant'), ('Business Casual'), ('Office Wear'), ('Bohemian'), ('Casual Chic'), ('Smart Casual'), ('Rocky'), ('Haute Couture'), ('Luxury'), ('Cozy'), ('Urban'), ('Summer Vibes'), ('Fall Layers'), ('Winter Ready'), ('Monochrome'), ('Playful'), ('Bold'), ('Preppy Chic'), ('Formal'), ('Resort Wear'), ('Athletically Inspired'), ('Urban Street'), ('Festival'), ('Avant-Garde'), ('Layered'), ('High Fashion'), ('Utility'), ('Contemporary'), ('Haute Street'), ('Techwear'), ('Japanese Street Style'), ('British Mod'), ('California Casual'), ('Cozy Chic'), ('Feminine Edge'), ('Cozy Luxe'), ('Power Dressing'), ('Smart Relaxed'), ('Office Glam'), ('Summer Casual'), ('Cold Weather Chic'), ('Edgy'), ('All-Black'), ('Color-Blocking'), ('Day-to-Night'), ('Country Style'), ('Nautical'), ('Loungewear'), ('Festival Boho'), ('Western'), ('High Street'), ('Sophisticated Casual'), ('Luxe Street Style'), ('Relaxed'), ('Street Luxe'), ('Boyish'), ('Fitted'), ('Casual Street Style'), ('Tech-Inspired'), ('Party Ready'), ('Resort Chic'), ('Runway Ready'), ('Luxe Minimalism'), ('College Student'), ('Vacation Ready'), ('Country Chic'), ('Mod'), ('Statement Pieces'), ('Night Out'), ('Sporty Chic'), ('Summer Breeze'), ('Graphic T-Shirt Casual'), ('Cottagecore'), ('Power Casual'), ('Retro Futuristic'), ('Outdoor Adventurer'), ('Preppy Casual'), ('Smart Tailored');

CREATE INDEX idx_user_username ON user (username);

----- V2 -----

CREATE TABLE `event_log` (
  `id` varchar(36) NOT NULL,
  `event` varchar(256) NOT NULL,
  `payload` varchar(4096) DEFAULT NULL,
  `created_date` datetime NOT NULL,
  `produced_by` int DEFAULT NULL,
  `retry_count` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `event_retry_count_idx` (`retry_count`),
  KEY `event_created_date_idx` (`created_date`),
  KEY `idx_event_log_event_class` (`event`)
);

CREATE TABLE `shedlock` (
  `name` varchar(64) NOT NULL,
  `lock_until` timestamp(3) NULL DEFAULT NULL,
  `locked_at` timestamp(3) NULL DEFAULT NULL,
  `locked_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
);

CREATE TABLE `user_subscription` (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11) NOT NULL,
    subscription_plan VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    app_account_token VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_subscription_user_id (user_id),
    INDEX idx_subscription_app_account_token (app_account_token)
);

CREATE TABLE `apple_notification` (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    notification_id VARCHAR(255) NOT NULL,
    notification_type VARCHAR(255),
    subtype VARCHAR(255),
    notification_version VARCHAR(255),
    signed_transaction_info TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_apple_notification_id (notification_id)
);

CREATE TABLE `apple_transaction` (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11) NOT NULL,
    subscription_plan VARCHAR(255),
    original_transaction_id VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    purchase_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_apple_transaction_id (transaction_id)
);

CREATE TABLE `user_credit` (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11) NOT NULL,
    credit INT(5) NOT NULL DEFAULT 0,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid BOOLEAN DEFAULT TRUE,
    transaction_id INT(11),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (transaction_id) REFERENCES apple_transaction(id)
); 

CREATE INDEX idx_user_credit_expires_at ON user_credit(expires_at);

ALTER TABLE `user_credit` ADD COLUMN `credit_type` VARCHAR(255) NOT NULL;

ALTER TABLE `user_subscription` ADD COLUMN `transaction_pending` BOOLEAN DEFAULT FALSE;

CREATE TABLE `client_fingerprint` (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    nonce VARCHAR(255) NOT NULL UNIQUE,
    user_fingerprint VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_date TIMESTAMP NOT NULL,
    INDEX idx_client_fing_nonce (nonce),
    INDEX idx_client_fing_user_fingerprint (user_fingerprint)
);

CREATE TABLE `referral_link` (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    nonce VARCHAR(25) NOT NULL UNIQUE,
    user_id INT(11) NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    redeemed_at TIMESTAMP NULL,
    expiration_date TIMESTAMP NOT NULL,
    INDEX idx_referral_link_user_id (user_id),
    INDEX idx_referral_link_nonce (nonce)
);

ALTER TABLE `user` ADD COLUMN `device_token` VARCHAR(255) NULL;

CREATE TABLE `push_notification` (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(25) NOT NULL,
    user_id INT(11) NOT NULL,
    content JSON NOT NULL,
    is_consumed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_notification_user_id (user_id)
);

ALTER TABLE `gpt_prompt` ADD COLUMN `submission_type` VARCHAR(255) NOT NULL DEFAULT 'SINGLE';
ALTER TABLE `ai_feedback` ADD COLUMN `submission_type` VARCHAR(255) NOT NULL DEFAULT 'SINGLE';

INSERT INTO gpt_prompt (prompt, submission_type, image_type, active) VALUES (
    'Please analyze the two attached images of individuals wearing different outfits. Provide honest, constructive, side-by-side feedback that highlights strengths and areas for improvement for each ensemble. Imagine you are a professional stylist known for realistic, practical advice. Use a friendly yet straightforward tone. Strictly adhere to the word limits for each section; if any section does not meet the requirements, rephrase and retry until it does.” Outfit Styles: For each ensemble, describe its style using three neutral words. Present your answer as a unified statement referencing each option by its distinctive elements. Example: “The white shirt with skirt is classic, clean, minimal; the brown cropped blazer paired with a pleated skirt is modern, edgy, bold.” Style Match: Analyze how each outfit aligns with my style preferences (${style1}, ${style2}, ${style3}). Provide a unified response with clearly separated segments for each option (using descriptive references), each segment containing exactly 14-18 words. Example: “While the white shirt and skirt reflects ${style1}, the brown blazer with skirt resonates with ${style2} and ${style3}; the latter is closer to your style.” Occasion Fit: Evaluate the practicality of each ensemble for the ${occasion}. Provide a single unified statement with clearly separated segments for each outfit, each segment containing exactly 14-18 words, explaining why it works or suggesting alternatives if necessary. Trend Alert: Assess the trendiness of each outfit by considering ${date}, ${location}, ${weather} (and any additional style cues). Offer one unified response that includes clearly separated segments for each ensemble, each in exactly 14-18 words, noting any outdated elements or seasonal mismatches. Color Preference: For each outfit, list the primary and secondary colors and comment on any clashes with my preferences. Provide one unified statement with clearly separated segments for each option, each segment exactly 8-10 words. Enhancement Recommendations: Offer realistic tips for improvement (e.g., weather adjustments, better accessories) for each ensemble. Provide a unified statement with clearly separated suggestions for each outfit, with each tip in exactly 2-3 words. Example: “White set: Add scarf; Brown ensemble: Try hat.” Hair Advice: Suggest a hairstyle tweak for each outfit if needed. In one unified response, offer clearly separated suggestions that complement each ensemble’s features, each suggestion in exactly 2-3 words. Example: “White set: Soft waves; Brown ensemble: Sleek bun.” Winner Determination: Based on your evaluations, decide which outfit wins. Provide a unified explanation that focuses solely on the winning outfit—addressing its style, occasion fit, trends, and overall practicality—in exactly 14-18 words. Example: “The brown cropped blazer with pleated skirt wins due to its trend alignment, occasion suitability, and overall style.” Summary: Provide a brief overall comparison of the two ensembles in a single unified statement. Balance praise with constructive tips and clearly emphasize the strengths of the winning outfit while referencing distinctive elements from both. Final Compliment: End with a final compliment directed only to the winning outfit, in exactly 2-3 words. Example: “Absolutely stunning!', 
    'COMPARE', 
    'BODY', 
    true
);

-- New Body Prompt --
UPDATE `gpt_prompt` SET active = 0 WHERE submission_type = 'SINGLE' AND image_type = 'BODY' AND active = 1;
INSERT INTO gpt_prompt (prompt, submission_type, image_type, active) VALUES (
    'Please analyze the attached image of a person''s head and upper body, including hairstyle, accessories, and facial features, providing a friendly, detailed evaluation of how everything works together. You are now best friend of me. Respond with a warm, supportive, and encouraging tone. 1. Head Style: Describe the overall look of the hair and accessories in three short words. (May be wearing a hat) 2. Style and Face Fit: Evaluate whether this head style matches my style (${style 1}, ${style 2}, ${style 3}) (if there are stylies info given). Or (4-5 words) Briefly comment on how the hairstyle and accessories complement the face shape (e.g., oval, round, square) and suit the wearer’s overall style. 3. Occasion Fit: Assess whether the head style is appropriate for the ${occasion} (if there is occasion info given) . Or (10-14 words) suggest where this head style would work best (e.g., formal event, casual day out, professional setting) and why it suits the occasion. 4. Trend Alert: (6-8 words) Comment on how trendy the hairstyle and accessories are (consider seasonality and any current trends for ${local} (if there is location info given) on ${date}). 5. Detailed Elements: List any visible hair accessories (e.g., clips, headbands), makeup (if visible), or jewelry, and explain how they fit with the style. Each item is numbered (①②③...). 6. Color Preference: Analyze the outfit''s color scheme to determine compatibility with the ${color 1}, ${color 2} and ${color 3} (if there are colors info given). Or briefly extract and list the primary and secondary colors of the hairstyle and accessories, providing associated color codes (e.g., \"Dark Brown - #4B3F29\" for hair or \"Gold - #FFD700\" for accessories). Limit to 2 words per color. 7. Enhancement Recommendations: Offer 1 to 3 quick tips (2-3 words each), friendly tips to tweak the hairstyle or accessories. Keep it simple and seasonal (e.g., weather considerations, hair texture tips for ${local} (if there is location info given) on ${date}). 8. Hair Advice: (2 or 3 words) If needed, suggest a hairstyle change that could elevate the look or better complement the face shape. If everything looks great, just give a compliment on how well the hair suits the outfit. 9. Coordinate Recommendations: Return the precise X, Y coordinates for each hair accessory or facial accessory item (numbered as per the requirements above), starting from the top and moving downward and do same for potential enhancement recommendations locations as well (should not be in the center of the image) , based on a mobile screen size 1080x1920 pixels ,assuming the image is centered both horizontally and vertically. Provide a concise and meaningful summary of their look. The tone should feel warm, friendly, and relatable—like a supportive best friend giving fashion advice. 10. Summary: Provide a concise, friendly, and uplifting summary of the head-related aspects. Include a few key takeaways: Impression: How the hair and accessories come together for a harmonious look. Suitability: Mention how the hairstyle works for the occasion or the overall vibe. Personal Reflection: Acknowledge how the hairstyle and accessories reflect the user’s personal style and flair. Enhancements (if needed): Suggest any minor tweaks to improve the overall look. Compliment: End with a positive, confident note to make the user feel great about their head styling choice! Make the response short and meaningful (20 words max), balancing practical advice with friendly encouragement 11. Lastly add a confident and uplifting compliment in 2-3 words to make the wearer feel great about their look.', 
    'SINGLE', 
    'BODY', 
    true
);

// NEW
CREATE TABLE ai_feedback_image_paths (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ai_feedback_id INT NOT NULL,
    image_position INT NOT NULL,
    raw_image_path VARCHAR(255) NOT NULL,
    extracted_image_path VARCHAR(255) NOT NULL,
    CONSTRAINT fk_ai_feedback 
        FOREIGN KEY (ai_feedback_id) 
        REFERENCES ai_feedback(id) 
        ON DELETE CASCADE
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