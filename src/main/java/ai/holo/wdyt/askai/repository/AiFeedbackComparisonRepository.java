package ai.holo.wdyt.askai.repository;

import ai.holo.wdyt.askai.model.entity.AiComparisonFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiFeedbackComparisonRepository extends JpaRepository<AiComparisonFeedback, Long> {

    void deleteAllByUserId(Long userId);

    Optional<AiComparisonFeedback> findFirstByUserIdOrderByCreatedAtDesc(Long id);
}
