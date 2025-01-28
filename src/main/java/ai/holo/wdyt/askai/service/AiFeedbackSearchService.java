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


    public Page<AiFeedback> findAiFeedbacksByTags(Long userId, Map<String, List<String>> tagFilters, Boolean liked,
                                                  Long excludeUserId, Pageable pageable) {
        // Build the base query strings
        String baseQuery = "SELECT af.* FROM ai_feedback af";
        String whereClause = buildWhereClause(tagFilters, liked, excludeUserId);

        // Add sorting from the Pageable object
        StringBuilder queryString = new StringBuilder(baseQuery).append(whereClause);
        addSorting(pageable, queryString);

        // Create the main query
        Query query = entityManager.createNativeQuery(queryString.toString(), AiFeedback.class);
        setQueryParameters(query, userId, tagFilters, liked, excludeUserId);

        // Set pagination parameters
        int firstResult = pageable.getPageNumber() * pageable.getPageSize();
        query.setFirstResult(firstResult);
        query.setMaxResults(pageable.getPageSize());

        // Execute the query and return the result
        List<AiFeedback> results = query.getResultList();

        // Get total count for pagination
        String countQueryString = "SELECT COUNT(*) FROM ai_feedback af" + whereClause;
        Query countQuery = entityManager.createNativeQuery(countQueryString);
        setQueryParameters(countQuery, userId, tagFilters, liked, excludeUserId);

        long totalCount = ((Number) countQuery.getSingleResult()).longValue();
        return new PageImpl<>(results, pageable, totalCount);
    }

    private String buildWhereClause(Map<String, List<String>> tagFilters, Boolean liked, Long excludeUserId) {
        StringBuilder whereClause = new StringBuilder(" WHERE af.user_id = :userId");

        if (liked != null) {
            whereClause.append(" AND af.like_style = :liked");
        }

        if (excludeUserId != null) {
            whereClause.append(" AND (af.feedback_entries IS NULL OR NOT JSON_CONTAINS(af.feedback_entries, JSON_OBJECT('userId', :excludeUserId), '$'))");
        }

        for (Map.Entry<String, List<String>> entry : tagFilters.entrySet()) {
            String tagCategory = entry.getKey();
            List<String> tags = entry.getValue();

            if (!tags.isEmpty()) {
                whereClause.append(" AND JSON_CONTAINS(af.tags, :")
                        .append(tagCategory)
                        .append(", '$.")
                        .append(tagCategory)
                        .append("') = 1");
            }
        }

        return whereClause.toString();
    }

    private void setQueryParameters(Query query, Long userId, Map<String, List<String>> tagFilters, Boolean liked, Long excludeUserId) {
        query.setParameter("userId", userId);

        if (liked != null) {
            query.setParameter("liked", liked);
        }

        if (excludeUserId != null) {
            query.setParameter("excludeUserId", excludeUserId);
        }

        for (Map.Entry<String, List<String>> entry : tagFilters.entrySet()) {
            String tagCategory = entry.getKey();
            List<String> tags = entry.getValue();

            if (!CollectionUtils.isEmpty(tags)) {
                String jsonArray = convertListToJsonArray(tags);
                query.setParameter(tagCategory, jsonArray);
            }
        }
    }

    private String convertListToJsonArray(List<String> list) {
        return list.stream()
                .map(tag -> "\"" + tag.replace("\"", "\\\"") + "\"") // Escape quotes in tags
                .collect(Collectors.joining(",", "[", "]"));
    }

    private void addSorting(Pageable pageable, StringBuilder queryString) {
        if (pageable.getSort().isSorted()) {
            queryString.append(" ORDER BY ");
            pageable.getSort().forEach(order -> {
                queryString.append("af.")
                        .append(order.getProperty())
                        .append(" ")
                        .append(order.getDirection().name())
                        .append(", ");
            });

            queryString.setLength(queryString.length() - 2); // Remove the last comma
        }
    }
}
