package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.UserFriendRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFriendRecordRepository extends JpaRepository<UserFriendRecord, Long> {

}
