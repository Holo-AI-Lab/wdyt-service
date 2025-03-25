package ai.holo.wdyt.askai.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "report_ai_feedback")
@Getter
@Setter
@NoArgsConstructor
public class ReportAiFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "ai_feedback_id")
    private Long aiFeedbackId;
    @Column(name = "ai_comparison_feedback_id")
    private Long aiComparisonFeedbackId;
    @Column(name = "ai_feedback_entry_id")
    private String aiFeedbackEntryId;
    @Column(name = "feedback")
    private String feedback;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private ReportAiFeedback(Long userId, String aiFeedbackEntryId, String feedback) {
        this.userId = userId;
        this.aiFeedbackEntryId = aiFeedbackEntryId;
        this.feedback = feedback;
        this.createdAt = LocalDateTime.now();
    }

    public static ReportAiFeedback fromAiFeedback(Long userId, Long aiFeedbackId, String aiFeedbackEntryId, String feedback) {
        ReportAiFeedback report = new ReportAiFeedback(userId, aiFeedbackEntryId, feedback);
        report.setAiFeedbackId(aiFeedbackId);
        return report;
    }

    public static ReportAiFeedback fromAiComparisonFeedback(Long userId, Long aiComparisonFeedbackId, String aiFeedbackEntryId, String feedback) {
        ReportAiFeedback report = new ReportAiFeedback(userId, aiFeedbackEntryId, feedback);
        report.setAiComparisonFeedbackId(aiComparisonFeedbackId);
        return report;
    }
}
