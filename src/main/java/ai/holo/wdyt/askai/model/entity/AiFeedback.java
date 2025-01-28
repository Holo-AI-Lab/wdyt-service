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
public class AiFeedback {
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
    @Column(name = "like_style")
    private boolean likeStyle;
    @Column(name = "top_list_order")
    private Integer topListOrder;
    @Column(name = "standard_order")
    private Integer order;
    @Convert(converter = TagConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, List<String>> tags = new HashMap<>();
    @Convert(converter = FeedbackEntryConverter.class)
    @Column(name = "feedback_entries", columnDefinition = "JSON")
    private List<FeedbackEntry> feedbackEntries = new ArrayList<>();

    public AiFeedback(Long userId, String rawImagePath,
                      ImageType imageType, String extractedImagePath, Integer topListOrder,
                      Integer order) {
        this.userId = userId;
        this.rawImagePath = rawImagePath;
        this.extractedImagePath = extractedImagePath;
        this.imageType = imageType;
        this.createdAt = LocalDateTime.now();
        this.topListOrder = topListOrder;
        this.order = order;
    }

    public void addFeedbackEntry(FeedbackEntry feedbackEntry) {
        feedbackEntries.add(feedbackEntry);
    }

    public void updateTags(Map<String, List<String>> newTags) {
        newTags.keySet().forEach(key -> {
            if (tags.containsKey(key)) {
                newTags.get(key).forEach(value -> {
                    if (!tags.get(key).contains(value)) {
                        tags.get(key).add(value);
                    }
                });
            } else {
                tags.put(key, newTags.get(key));
            }
        });
    }
}