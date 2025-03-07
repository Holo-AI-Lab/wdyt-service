package ai.holo.wdyt.askai.repository;

import ai.holo.wdyt.askai.model.entity.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {
    Optional<AiFeedback> findByIdAndUserId(Long id, Long userId);

    int countByUserIdAndTopListOrderIsNotNull(Long userId);
    List<AiFeedback> findByUserIdAndTopListOrderIsNotNullOrderByTopListOrderAsc(Long userId);

    void deleteAllByUserId(Long id);

    int countByUserId(Long userId);
}
