package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    Page<User> findByUsernameContainingIgnoreCaseAndIdNot(String userName, Long currentUserId, Pageable page);

    Stream<User> findAllByCreditBalanceLessThanEqualAndDeviceTokenIsNotNull(int i);
}
