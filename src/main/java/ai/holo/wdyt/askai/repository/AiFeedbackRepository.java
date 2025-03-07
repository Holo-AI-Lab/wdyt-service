package ai.holo.wdyt.askai.repository;

import ai.holo.wdyt.askai.model.entity.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {

    void deleteAllByUserId(Long id);

    int countByUserId(Long userId);
}
