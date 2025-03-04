package ai.holo.wdyt.askai.model.dto;

import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Taggable {

    Tag getTag();

    default Map<String, List<String>> getTags() {
        Map<String, List<String>> tags = new HashMap<>();
        if (!CollectionUtils.isEmpty(getTag().style())) {
            tags.put("style", getTag().style());
        }
        if (!CollectionUtils.isEmpty(getTag().occasion())) {
            tags.put("occasion", getTag().occasion());
        }
        if (!CollectionUtils.isEmpty(getTag().color())) {
            tags.put("color", getTag().color());
        }
        return tags;
    }
}
