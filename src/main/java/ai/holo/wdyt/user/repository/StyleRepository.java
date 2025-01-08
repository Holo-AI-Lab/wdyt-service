package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.Robot;
import ai.holo.wdyt.user.model.entity.Style;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StyleRepository extends JpaRepository<Style, Long> {
}
