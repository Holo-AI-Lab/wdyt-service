package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.model.entity.AiComparisonFeedback;
import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.user.model.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class AiFeedbackSearchService {

    private final EntityManager entityManager;

    public AiFeedbackSearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<String> findDistinctTagsFromAiFeedbackByUserIdAndTag(Long userId, String tag) {
        return getTagsFromAiFeedback("ai_feedback", userId, tag);
    }

    public List<String> findDistinctTagsFromAiComparisonFeedbackByUserIdAndTag(Long userId, String tag) {
        return getTagsFromAiFeedback("ai_comparison_feedback", userId, tag);
    }

    public List<String> findDistinctTagsFromAiFeedbackAndComparisonByUserIdAndTag(Long userId, String tag) {
        List<String> tagsFromAiFeedback = getTagsFromAiFeedback("ai_feedback", userId, tag);
        List<String> tagsFromComparison = getTagsFromAiFeedback("ai_comparison_feedback", userId, tag);
        return Stream.concat(tagsFromAiFeedback.stream(), tagsFromComparison.stream())
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }

    private List<String> getTagsFromAiFeedback(String table, Long userId, String tag) {
        String queryString = String.format(
                "SELECT tag " +
                        "FROM %s, " +
                        "JSON_TABLE(tags->'$.%s', '$[*]' COLUMNS(tag TEXT PATH '$')) AS distinct_tags " +
                        "WHERE user_id = :userId " +
                        "GROUP BY tag " +
                        "ORDER BY COUNT(tag) DESC",
                table, tag
        );

        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("userId", userId);

        @SuppressWarnings("unchecked")
        List<String> result = query.getResultList();
        return result;
    }

    public List<String> getStylesBasedOnUserStyleAdaptedPreference(User aiUser) {
        if (aiUser.isStyleAdapted()) {
            List<String> userMostUsedStyles = findDistinctTagsFromAiFeedbackAndComparisonByUserIdAndTag(aiUser.getId(), "style");
            return userMostUsedStyles.subList(0, Math.min(3, userMostUsedStyles.size()));
        }
        return aiUser.getSelectedStyle() != null ? aiUser.getSelectedStyle().styles() : List.of();
    }


    public Page<AiComparisonFeedback> findAiComparisonFeedbacksByTags(Long userId, Map<String, List<String>> tagFilters,
                                                                      Boolean liked, PageRequest pageRequestWithSort) {
        return findFeedbacksByTags(AiComparisonFeedback.class, userId,
                tagFilters, liked, null, null, null, null, pageRequestWithSort);
    }

    public Page<AiFeedback> findAiFeedbacksByTags(Long userId, Map<String, List<String>> tagFilters, Boolean liked,
                                                  Boolean wardrobeItemExtracted, Long feedbackIdForComparison, List<Long> idsNot, ImageType imageType,
                                                  Pageable pageable) {
        return findFeedbacksByTags(AiFeedback.class, userId,
                tagFilters, liked, wardrobeItemExtracted, feedbackIdForComparison, idsNot, imageType, pageable);
    }

    private <T> Page<T> findFeedbacksByTags(Class<T> entityClass, Long userId, Map<String, List<String>> tagFilters,
                                                  Boolean liked, Boolean wardrobeItemExtracted, Long feedbackIdForComparison,
                                                  List<Long> idsNot, ImageType imageType, Pageable pageable) {
        String tableName = getTableName(entityClass);

        // Build the base query string dynamically based on the table name
        String baseQuery = "SELECT af.* FROM " + tableName + " af";

        if (feedbackIdForComparison != null) {
            // feedbackIdForComparison is used to exclude feedbacks that have already been compared with the given feedback
            // We'll add this id to notIds list to exclude the feedback itself from the results
            idsNot = Stream.concat(
                    Optional.ofNullable(idsNot).orElseGet(ArrayList::new).stream(),
                    Stream.of(feedbackIdForComparison)
            ).collect(Collectors.toList());
        }
        String whereClause = buildWhereClause(tagFilters, liked, wardrobeItemExtracted, feedbackIdForComparison, idsNot, imageType);

        // Construct the full query
        StringBuilder queryString = new StringBuilder(baseQuery).append(whereClause);
        addSorting(pageable, queryString);

        // Create the main query
        Query query = entityManager.createNativeQuery(queryString.toString(), entityClass);
        setQueryParameters(query, userId, tagFilters, liked, wardrobeItemExtracted, feedbackIdForComparison, idsNot, imageType);

        // Set pagination parameters
        int firstResult = pageable.getPageNumber() * pageable.getPageSize();
        query.setFirstResult(firstResult);
        query.setMaxResults(pageable.getPageSize());

        // Execute the query and return the result
        List<T> results = query.getResultList();

        // Get total count for pagination
        String countQueryString = "SELECT COUNT(*) FROM " + tableName + " af" + whereClause;
        Query countQuery = entityManager.createNativeQuery(countQueryString);
        setQueryParameters(countQuery, userId, tagFilters, liked, wardrobeItemExtracted, feedbackIdForComparison, idsNot, imageType);

        long totalCount = ((Number) countQuery.getSingleResult()).longValue();
        return new PageImpl<>(results, pageable, totalCount);
    }

    private <T> String getTableName(Class<T> entityClass) {
        Entity entity = entityClass.getAnnotation(Entity.class);
        if (entity != null && !entity.name().isEmpty()) {
            return entity.name();
        }
        throw new IllegalArgumentException("Entity class does not have an @Entity name: " + entityClass.getSimpleName());
    }

    private String buildWhereClause(Map<String, List<String>> tagFilters, Boolean liked, Boolean wardrobeItemExtracted, Long feedbackIdForComparison, List<Long> idsNot, ImageType imageType) {
        StringBuilder whereClause = new StringBuilder(" WHERE af.user_id = :userId");

        if (liked != null) {
            whereClause.append(" AND af.like_style = :liked");
        }

        if (wardrobeItemExtracted != null) {
            whereClause.append(" AND af.wardrobe_item_extracted = :wardrobeItemExtracted");
        }

        if (feedbackIdForComparison != null) {
            whereClause.append("""
                 AND id NOT IN (
                    SELECT ai_feedback_id1 FROM ai_comparison_feedback WHERE ai_feedback_id2 = :feedbackIdForComparison
                    UNION
                    SELECT ai_feedback_id2 FROM ai_comparison_feedback WHERE ai_feedback_id1 = :feedbackIdForComparison
                 )
                """.replaceAll("\\n", ""));
        }

        if (!CollectionUtils.isEmpty(idsNot)) {
            whereClause.append(" AND id NOT IN (:idsNot)");
        }

        if (imageType != null) {
            whereClause.append(" AND af.image_type = :imageType");
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

    private void setQueryParameters(Query query, Long userId, Map<String, List<String>> tagFilters, Boolean liked,
                                    Boolean wardrobeItemExtracted, Long feedbackIdForComparison, List<Long> idsNot, ImageType imageType) {
        query.setParameter("userId", userId);

        if (liked != null) {
            query.setParameter("liked", liked);
        }

        if (wardrobeItemExtracted != null) {
            query.setParameter("wardrobeItemExtracted", wardrobeItemExtracted);
        }

        if (feedbackIdForComparison != null) {
            query.setParameter("feedbackIdForComparison", feedbackIdForComparison);
        }

        if (!CollectionUtils.isEmpty(idsNot)) {
            String idsNotParameter = idsNot.stream().map(String::valueOf).collect(Collectors.joining(","));
            query.setParameter("idsNot", idsNotParameter);
        }

        if (imageType != null) {
            query.setParameter("imageType", imageType.name());
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

    public int countAiFeedbackByUserId(Long userId) {
        String queryString = "SELECT COUNT(af.id) FROM ai_feedback af WHERE af.user_id = :userId";
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("userId", userId);

        Number result = (Number) query.getSingleResult();
        return result != null ? result.intValue() : 0;
    }

    public int countReceivedAiFeedbackEntriesByUserId(Long userId) {
        String queryString = "SELECT COALESCE(SUM(JSON_LENGTH(feedback_entries)), 0) FROM ai_feedback WHERE user_id = :userId";
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("userId", userId);

        Number result = (Number) query.getSingleResult();
        return result != null ? result.intValue() : 0;
    }
}
