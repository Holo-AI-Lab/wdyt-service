package ai.holo.wdyt.user.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "user_feedback")
@Getter
@Setter
@NoArgsConstructor
public class UserFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "feedback")
    private String feedback;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public UserFeedback(Long userId, String feedback) {
        this.userId = userId;
        this.feedback = feedback;
        createdAt = LocalDateTime.now();
    }
}
