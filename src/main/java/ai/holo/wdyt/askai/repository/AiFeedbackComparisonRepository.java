package ai.holo.wdyt.askai.repository;

import ai.holo.wdyt.askai.model.entity.AiComparisonFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiFeedbackComparisonRepository extends JpaRepository<AiComparisonFeedback, Long> {
}
