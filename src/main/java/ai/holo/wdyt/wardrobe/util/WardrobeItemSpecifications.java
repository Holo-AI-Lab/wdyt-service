package ai.holo.wdyt.wardrobe.util;

import ai.holo.wdyt.wardrobe.model.entity.WardrobeItem;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItemCategory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class WardrobeItemSpecifications {

    private WardrobeItemSpecifications() {
    }

    public static Specification<WardrobeItem> belongsToWardrobe(Long wardrobeId) {
        return (root, query, cb) -> cb.equal(root.get("wardrobe").get("id"), wardrobeId);
    }

    public static Specification<WardrobeItem> hasCategory(WardrobeItemCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<WardrobeItem> isLiked(boolean liked) {
        return (root, query, cb) -> cb.equal(root.get("liked"), liked);
    }

    public static Specification<WardrobeItem> hasAnyColor(List<String> colors) {
        return buildJsonArrayOrSpecification("$.colors", colors, true);
    }

    public static Specification<WardrobeItem> hasAnySeason(List<String> seasons) {
        return buildJsonArrayOrSpecification("$.seasons", seasons, false);
    }

    public static Specification<WardrobeItem> hasAnyType(List<String> types) {
        return buildJsonArrayOrSpecification("$.types", types, false);
    }

    private static Specification<WardrobeItem> buildJsonArrayOrSpecification(
            String jsonPath,
            List<String> values,
            boolean isColor
    ) {
        return (root, query, cb) -> {
            if (values == null || values.isEmpty()) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            for (String raw : values) {
                if (raw == null) {
                    continue;
                }
                String safeValue = raw.replace("\"", "").trim();
                if (safeValue.isEmpty()) {
                    continue;
                }

                String jsonValue = isColor
                        ? "{\"name\":\"" + safeValue + "\"}"
                        : "\"" + safeValue + "\"";

                predicates.add(
                        cb.equal(
                                cb.function(
                                        "JSON_CONTAINS",
                                        Boolean.class,
                                        root.get("tags"),
                                        cb.literal(jsonValue),
                                        cb.literal(jsonPath)
                                ),
                                true
                        )
                );
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
