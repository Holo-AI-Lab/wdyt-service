package ai.holo.wdyt.askai.model.entity;

import java.util.List;
import java.util.Map;

public interface TaggableEntity {
    Map<String, List<String>> getTags();

    default void updateTags(Map<String, List<String>> newTags) {
        newTags.keySet().forEach(key -> {
            if (getTags().containsKey(key)) {
                newTags.get(key).forEach(value -> {
                    if (!getTags().get(key).contains(value)) {
                        getTags().get(key).add(value);
                    }
                });
            } else {
                getTags().put(key, newTags.get(key));
            }
        });
    }
}
