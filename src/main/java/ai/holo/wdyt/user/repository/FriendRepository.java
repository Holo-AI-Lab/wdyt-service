package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    Page<Friend> findAllByUserIdAndFriendIdNotIn(Long id, Collection<Long> notIds, PageRequest pageRequest);

    @Query("SELECT f FROM user_friend f " +
            "JOIN f.friend fr " +
            "WHERE f.userId = :userId " +
            "AND ((:search IS NULL OR :search = '') OR " +
            "LOWER(fr.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(fr.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    @EntityGraph(attributePaths = {"friend"})
    Page<Friend> findAllByUserIdWithSearch(@Param("userId") Long userId,
                                           @Param("search") String search,
                                           Pageable pageable);

    @Query("SELECT f FROM user_friend f " +
            "JOIN f.friend fr " +
            "WHERE f.userId = :userId " +
            "AND fr.id NOT IN (:notIds) " +
            "AND ((:search IS NULL OR :search = '') OR " +
            "LOWER(fr.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(fr.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    @EntityGraph(attributePaths = {"friend"})
    Page<Friend> findAllByUserIdAndFriendIdNotInWithSearch(@Param("userId") Long userId,
                                                     @Param("notIds") Collection<Long> notIds,
                                                     @Param("search") String search,
                                                     Pageable pageable);

    @EntityGraph(attributePaths = {"friend"})
    List<Friend> findAllByUserId(Long id);

    boolean existsByUserIdAndFriendId(Long currentUserId, Long userId);

    int countByUserId(Long userId);
}
