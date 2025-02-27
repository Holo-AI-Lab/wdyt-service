package ai.holo.wdyt.askai.model.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "ai_feedback_image_paths")
@Getter
@Setter
@NoArgsConstructor
public class AiFeedbackImagePath{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_feedback_id", nullable = false)
    private AiFeedback aiFeedback;

    @Column(name = "image_position", nullable = false)
    private Integer imagePosition;

    @Column(name = "raw_image_path", nullable = false, length = 255)
    private String rawImagePath;

    @Column(name = "extracted_image_path", nullable = false, length = 255)
    private String extractedImagePath;


    public AiFeedbackImagePath(AiFeedback feedback, int imagePosition, String rawImagePath, String extractedImagePath) {
        this.aiFeedback = feedback;
        this.imagePosition = imagePosition;
        this.rawImagePath = rawImagePath;
        this.extractedImagePath = extractedImagePath;
    }
}