package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    void deleteAllByUserId(Long id);
    void deleteAllByFriendId(Long id);

    Optional<Friend> findByUserIdAndFriendId(Long id, Long friendId);

    @EntityGraph(attributePaths = {"friend"})
    Page<Friend> findAllByUserId(Long id, PageRequest pageRequest);
    @EntityGraph(attributePaths = {"friend"})
    Page<Friend> findAllByUserIdAndIdNotIn(Long id, Collection<Long> notIds, PageRequest pageRequest);
    @EntityGraph(attributePaths = {"friend"})
    List<Friend> findAllByUserId(Long id);

    boolean existsByUserIdAndFriendId(Long currentUserId, Long userId);

    int countByUserId(Long userId);
}
