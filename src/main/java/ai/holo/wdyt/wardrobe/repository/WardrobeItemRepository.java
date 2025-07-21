package ai.holo.wdyt.wardrobe.repository;

import ai.holo.wdyt.wardrobe.model.entity.WardrobeItem;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, Long>, JpaSpecificationExecutor<WardrobeItem> {
    Page<WardrobeItem> findByWardrobeId(Long wardrobeId, Pageable pageable);

    Page<WardrobeItem> findByWardrobeIdAndCategory(Long id, WardrobeItemCategory categoryEnum, Pageable pageable);

    void deleteAllByWardrobeId(Long id);
}
