package ai.holo.wdyt.wardrobe.repository;

import ai.holo.wdyt.wardrobe.model.entity.Wardrobe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WardrobeRepository extends JpaRepository<Wardrobe, Long> {
    Optional<Wardrobe> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
