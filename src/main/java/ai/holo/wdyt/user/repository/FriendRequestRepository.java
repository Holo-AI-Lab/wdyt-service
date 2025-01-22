package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    void deleteAllByUserId(Long id);
    void deleteAllByFriendId(Long id);
    Page<FriendRequest> findAllByFriendId(Long id, PageRequest pageRequest);
    List<FriendRequest> findAllByUserId(Long id);

    Optional<Object> findByUserIdAndFriendId(Long id, Long friendId);
}
