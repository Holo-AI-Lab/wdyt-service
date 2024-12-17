package ai.holo.wdyt.askai.repository;

import ai.holo.wdyt.askai.model.entity.AiFeedbackOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiFeedbackOrderRepository extends JpaRepository<AiFeedbackOrder, Long> {
    Optional<AiFeedbackOrder> findByUserId(Long userId);

    void deleteAllByUserId(Long id);
}
