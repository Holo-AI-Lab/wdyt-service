package ai.holo.wdyt.wardrobe.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "report_wardrobe")
@Getter
@Setter
@NoArgsConstructor
public class ReportWardrobe {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(name = "user_id")
        private Long userId;
        @Column(name = "wardrobe_item_id")
        private Long wardrobeItemId;
        @Column(name = "feedback")
        private String feedback;
        @Column(name = "created_at")
        private LocalDateTime createdAt;

        public ReportWardrobe(Long userId, Long wardrobeItemId, String feedback) {
            this.userId = userId;
            this.wardrobeItemId = wardrobeItemId;
            this.feedback = feedback;
            this.createdAt = LocalDateTime.now();
        }
    }
