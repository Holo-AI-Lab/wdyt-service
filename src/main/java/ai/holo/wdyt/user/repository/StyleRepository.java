package ai.holo.wdyt.user.repository;

import ai.holo.wdyt.user.model.entity.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StyleRepository extends JpaRepository<Style, Long> {

    @Query("SELECT s FROM ai.holo.wdyt.user.model.entity.Style s WHERE " +
            "(:searchText IS NULL OR :searchText = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<Style> searchByFreeText(@Param("searchText") String searchText);
}
