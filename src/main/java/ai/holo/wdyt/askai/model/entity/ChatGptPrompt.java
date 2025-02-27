package ai.holo.wdyt.askai.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "gpt_prompt")
@Getter
@Setter
@NoArgsConstructor
public class ChatGptPrompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "prompt")
    private String prompt;
    @Column(name = "image_type")
    @Enumerated(EnumType.STRING)
    private ImageType imageType;
    @Column(name = "submission_type")
    @Enumerated(EnumType.STRING)
    private SubmissionType submissionType;
    @Column(name = "active")
    private boolean active;
}
