package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.entity.AiFeedback;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiFeedbackSearchService {

    private final EntityManager entityManager;

    public AiFeedbackSearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<String> findDistinctTagsByUserIdAndTag(Long userId, String tag) {
        String queryString = String.format(
                "SELECT tag " +
                        "FROM ai_feedback, " +
                        "JSON_TABLE(tags->'$.%s', '$[*]' COLUMNS(tag TEXT PATH '$')) AS distinct_tags " +
                        "WHERE user_id = :userId " +
                        "GROUP BY tag " +
                        "ORDER BY COUNT(tag) DESC",
                tag
        );

        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("userId", userId);

        @SuppressWarnings("unchecked")
        List<String> result = query.getResultList();
        return result;
    }


    public Page<AiFeedback> findAiFeedbacksByTags(Long userId, Map<String, List<String>> tagFilters, Pageable pageable) {
        // Build the base query string
        StringBuilder queryString = new StringBuilder("SELECT af.* FROM ai_feedback af WHERE af.user_id = :userId");

        // Loop through the tag filters (e.g., color, style, occasion)
        for (Map.Entry<String, List<String>> entry : tagFilters.entrySet()) {
            String tagCategory = entry.getKey();  // e.g., "color", "style", "occasion"
            List<String> tags = entry.getValue(); // e.g., ["red", "blue"]

            if (!tags.isEmpty()) {
                // Add AND condition for each tag category
                queryString.append(" AND JSON_CONTAINS(af.tags, :")
                        .append(tagCategory)
                        .append(", '$.")
                        .append(tagCategory)
                        .append("') = 1");
            }
        }

        // Add sorting from the Pageable object
        addSorting(pageable, queryString);

        // Create the query
        Query query = entityManager.createNativeQuery(queryString.toString(), AiFeedback.class);

        // Set the userId parameter
        query.setParameter("userId", userId);

        // Bind each tag list as a JSON string dynamically
        for (Map.Entry<String, List<String>> entry : tagFilters.entrySet()) {
            String tagCategory = entry.getKey();
            List<String> tags = entry.getValue();

            if (CollectionUtils.isEmpty(tags)) {
                continue;
            }
            // Convert the list of tags to a JSON array string
            String jsonArray = convertListToJsonArray(tags);
            query.setParameter(tagCategory, jsonArray);
        }

        // Set pagination parameters
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int firstResult = pageNumber * pageSize;
        query.setFirstResult(firstResult);
        query.setMaxResults(pageSize);

        // Execute the query and return the result
        List<AiFeedback> results = query.getResultList();

        // Get total count for pagination
        long totalCount = getTotalCount(userId, tagFilters);

        // Return a Page object with results and total count
        return new PageImpl<>(results, pageable, totalCount);
    }

    private String convertListToJsonArray(List<String> list) {
        return list.stream()
                .map(tag -> "\"" + tag.replace("\"", "\\\"") + "\"") // Escape quotes in tags
                .collect(Collectors.joining(",", "[", "]"));
    }

    private long getTotalCount(Long userId, Map<String, List<String>> tagFilters) {
        StringBuilder countQueryString = new StringBuilder("SELECT COUNT(*) FROM ai_feedback af WHERE af.user_id = :userId");

        for (Map.Entry<String, List<String>> entry : tagFilters.entrySet()) {
            String tagCategory = entry.getKey();
            List<String> tags = entry.getValue();

            if (!tags.isEmpty()) {
                countQueryString.append(" AND JSON_CONTAINS(af.tags, :")
                        .append(tagCategory)
                        .append(", '$.")
                        .append(tagCategory)
                        .append("') = 1");
            }
        }

        Query countQuery = entityManager.createNativeQuery(countQueryString.toString());
        countQuery.setParameter("userId", userId);

        // Bind each tag list as a JSON string dynamically
        for (Map.Entry<String, List<String>> entry : tagFilters.entrySet()) {
            String tagCategory = entry.getKey();
            List<String> tags = entry.getValue();

            if (CollectionUtils.isEmpty(tags)) {
                continue;
            }
            // Convert the list of tags to a JSON array string
            String jsonArray = convertListToJsonArray(tags);
            countQuery.setParameter(tagCategory, jsonArray);
        }

        return ((Number) countQuery.getSingleResult()).longValue();
    }

    private void addSorting(Pageable pageable, StringBuilder queryString) {
        // Add sorting from the Pageable object
        if (pageable.getSort().isSorted()) {
            queryString.append(" ORDER BY ");
            pageable.getSort().forEach(order -> {
                queryString.append("af.")
                        .append(order.getProperty())
                        .append(" ")
                        .append(order.getDirection().name())
                        .append(", ");
            });

            // Remove the last comma
            queryString.setLength(queryString.length() - 2);
        }
    }

}
