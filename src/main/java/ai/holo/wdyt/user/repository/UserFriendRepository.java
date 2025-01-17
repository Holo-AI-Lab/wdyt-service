package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.model.entity.UserFriend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserFriendRepository extends JpaRepository<UserFriend, Long> {
    @Query("select u FROM( " +
            "select userRId as userId FROM ai.holo.wdyt.user.model.entity.UserFriend where userLId = ?1 and status=?2 " +
            "UNION ALL " +
            "select userLId as userId FROM ai.holo.wdyt.user.model.entity.UserFriend where userRId = ?1 and status=?2 " +
            ") uf join ai.holo.wdyt.user.model.entity.User u on uf.userId=u.id where (?3 IS NULL OR u.username = ?3)")
    Page<User> findFriendWithUser(Long userId, String status, String userName, PageRequest pageRequest);

    UserFriend findByUserLRIdAndStatus(String userLRId, String status);

    @Query("select u FROM ai.holo.wdyt.user.model.entity.UserFriend uf " +
            "join ai.holo.wdyt.user.model.entity.User u on uf.userLId=u.id where uf.userRId = ?1 and status=?2")
    Page<User> findByUserRIdAndStatus(Long id, String status, PageRequest pageRequest);
}
