package ai.holo.wdyt.wardrobe.repository;

import ai.holo.wdyt.wardrobe.model.entity.DraftWardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DraftWardrobeItemRepository extends JpaRepository<DraftWardrobeItem, Long>{
    List<DraftWardrobeItem> findByAiFeedbackId(Long aiFeedbackId);
    void deleteByUserId(Long userId);
}
