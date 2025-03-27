package ai.holo.wdyt.askai.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(name = "ai_feedback")
@Getter
@Setter
@NoArgsConstructor
public class AiFeedback implements TaggableEntity, FeedbackReceiverEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "raw_image_path")
    private String rawImagePath;
    @Column(name = "extracted_image_path")
    private String extractedImagePath;
    @Column(name = "image_type")
    @Enumerated(EnumType.STRING)
    private ImageType imageType;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "last_feedback_received_at")
    private LocalDateTime lastFeedbackReceivedAt;
    @Column(name = "like_style")
    private boolean likeStyle;
    @Convert(converter = TagConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, List<String>> tags = new HashMap<>();
    @Convert(converter = FeedbackEntryConverter.class)
    @Column(name = "feedback_entries", columnDefinition = "JSON")
    private List<FeedbackEntry> feedbackEntries = new ArrayList<>();

    public AiFeedback(Long userId, String rawImagePath,
                      ImageType imageType, String extractedImagePath) {
        this.userId = userId;
        this.rawImagePath = rawImagePath;
        this.extractedImagePath = extractedImagePath;
        this.imageType = imageType;
        this.createdAt = LocalDateTime.now();
    }
}