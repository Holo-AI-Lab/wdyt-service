package ai.holo.wdyt.wardrobe.util;

import ai.holo.wdyt.wardrobe.model.entity.WardrobeItem;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WardrobeItemSpecifications {

    public static Specification<WardrobeItem> hasAnyColor(List<String> colors) {
        return buildJsonArrayOrSpecification("$.colors", colors, true);
    }

    public static Specification<WardrobeItem> hasAnySeason(List<String> seasons) {
        return buildJsonArrayOrSpecification("$.seasons", seasons, false);
    }

    public static Specification<WardrobeItem> hasAnyType(List<String> types) {
        return buildJsonArrayOrSpecification("$.types", types, false);
    }

    public static Specification<WardrobeItem> isLiked() {
        return (root, query, cb) -> cb.isTrue(root.get("liked"));
    }

    private static Specification<WardrobeItem> buildJsonArrayOrSpecification(String jsonPath, List<String> values, boolean isColor) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (String value : values) {
                String safeValue = value.replace("\"", ""); // Basic sanitize
                String jsonValue = isColor
                        ? "{\"name\":\"" + safeValue + "\"}"   // For color objects
                        : "\"" + safeValue + "\"";            // For strings

                predicates.add(cb.equal(
                        cb.function("JSON_CONTAINS", Boolean.class,
                                root.get("tags"),
                                cb.literal(jsonValue),
                                cb.literal(jsonPath)
                        ), true
                ));
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
