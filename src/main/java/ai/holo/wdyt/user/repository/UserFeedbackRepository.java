package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    void deleteAllByUserId(Long id);
}
