package ai.holo.wdyt.wardrobe.repository;

import ai.holo.wdyt.wardrobe.model.entity.WardrobeItem;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, Long>, JpaSpecificationExecutor<WardrobeItem> {
    @Query(value = """
            SELECT DISTINCT JSON_UNQUOTE(JSON_EXTRACT(j.value, '$.name')) AS colorName,
                            JSON_UNQUOTE(JSON_EXTRACT(j.value, '$.code')) AS colorCode
            FROM wardrobe_item,
                 JSON_TABLE(tags->'$.colors', '$[*]' COLUMNS (value JSON PATH '$')) j
            WHERE wardrobe_id = :wardrobeId
            """, nativeQuery = true)
    List<Object[]> findDistinctColors(@Param("wardrobeId") Long wardrobeId);

    @Query(value = """
            SELECT DISTINCT JSON_UNQUOTE(value) AS season
            FROM wardrobe_item,
                 JSON_TABLE(tags->'$.seasons', '$[*]' COLUMNS (value VARCHAR(50) PATH '$')) j
            WHERE wardrobe_id = :wardrobeId
            """, nativeQuery = true)
    List<String> findDistinctSeasons(@Param("wardrobeId") Long wardrobeId);

    @Query(value = """
            SELECT DISTINCT JSON_UNQUOTE(value) AS type
            FROM wardrobe_item,
                 JSON_TABLE(tags->'$.types', '$[*]' COLUMNS (value VARCHAR(50) PATH '$')) j
            WHERE wardrobe_id = :wardrobeId
            """, nativeQuery = true)
    List<String> findDistinctTypes(@Param("wardrobeId") Long wardrobeId);

    void deleteAllByWardrobeId(Long id);

    @Query("SELECT DISTINCT w.category FROM wardrobe_item w WHERE w.wardrobe.id = :wardrobeId")
    List<WardrobeItemCategory> findDistinctCategories(@Param("wardrobeId") Long wardrobeId);
}
