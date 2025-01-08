package ai.holo.wdyt.askai.repository;

import ai.holo.wdyt.askai.model.entity.Occasion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OccasionRepository extends JpaRepository<Occasion, Long> {
    @Query("SELECT o FROM ai.holo.wdyt.askai.model.entity.Occasion o " +
            "WHERE (:searchText IS NULL OR :searchText = '' OR LOWER(o.name) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<Occasion> searchByFreeText(@Param("searchText") String searchText);

}
