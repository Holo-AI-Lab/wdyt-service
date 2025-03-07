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

@Entity(name = "ai_comparison_feedback")
@Getter
@Setter
@NoArgsConstructor
public class AiComparisonFeedback implements TaggableEntity, FeedbackReceiverEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "ai_feedback_id1")
    private Long aiFeedbackId1;
    @Column(name = "ai_feedback_id2")
    private Long aiFeedbackId2;
    @Column(name = "image_type")
    @Enumerated(EnumType.STRING)
    private ImageType imageType;
    @Column(name = "image1_path")
    private String image1Path;
    @Column(name = "image2_path")
    private String image2Path;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "like_style")
    private boolean likeStyle;
    @Column(name = "winner")
    private int winner;
    @Convert(converter = TagConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, List<String>> tags = new HashMap<>();
    @Convert(converter = FeedbackEntryConverter.class)
    @Column(name = "feedback_entries", columnDefinition = "JSON")
    private List<FeedbackEntry> feedbackEntries = new ArrayList<>();

    public AiComparisonFeedback(Long userId, Long feedback1, Long feedback2,
                                ImageType imageType, String image1Path, String image2Path,
                                int winner) {
        this.userId = userId;
        this.aiFeedbackId1 = feedback1;
        this.aiFeedbackId2 = feedback2;
        this.imageType = imageType;
        this.image1Path = image1Path;
        this.image2Path = image2Path;
        this.winner = winner;
        this.createdAt = LocalDateTime.now();
    }
}