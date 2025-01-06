package ai.holo.wdyt.askai.model.entity;

import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "ai_feedback")
@Getter
@Setter
@NoArgsConstructor
public class AiFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "prompt_id")
    private Long promptId;
    @Column(name = "response")
    private String response;
    @Column(name = "raw_image_path")
    private String rawImagePath;
    @Column(name = "extracted_image_path")
    private String extractedImagePath;
    @Column(name = "image_type")
    @Enumerated(EnumType.STRING)
    private ImageType imageType;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "like_style")
    private boolean likeStyle;
    @Column(name = "like_ai_response")
    private Boolean likeAiResponse;
    @Column(name = "top_list_order")
    private Integer topListOrder;
    @Column(name = "standard_order")
    private Integer order;
    @Convert(converter = LocationAndWeatherConverter.class)
    @Column(name = "location_and_weather")
    private LocationAndWeatherDto locationAndWeather;

    public AiFeedback(Long userId, Long promptId, String response, String rawImagePath,
                      ImageType imageType, String extractedImagePath, Integer topListOrder,
                      Integer order, LocationAndWeatherDto locationAndWeather) {
        this.userId = userId;
        this.promptId = promptId;
        this.response = response;
        this.rawImagePath = rawImagePath;
        this.extractedImagePath = extractedImagePath;
        this.imageType = imageType;
        this.createdAt = LocalDateTime.now();
        this.topListOrder = topListOrder;
        this.order = order;
        this.locationAndWeather = locationAndWeather;
    }
}
