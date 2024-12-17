package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.Robot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotRepository extends JpaRepository<Robot, Long> {
}
