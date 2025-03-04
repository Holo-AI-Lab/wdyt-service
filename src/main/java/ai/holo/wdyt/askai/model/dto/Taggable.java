package ai.holo.wdyt.askai.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Taggable {

    Tag getTag();

    @JsonIgnore
    default Map<String, List<String>> getTags() {
        Map<String, List<String>> tags = new HashMap<>();
        if (!CollectionUtils.isEmpty(getTag().style())) {
            tags.put("style", getTag().style());
        }
        if (!CollectionUtils.isEmpty(getTag().occasion())) {
            tags.put("occasion", getTag().occasion());
        }
        if (!CollectionUtils.isEmpty(getTag().color())) {
            tags.put("color", getTag().color().stream().map(Color::name).toList());
        }
        if (!CollectionUtils.isEmpty(getTag().color())) {
            tags.put("colorCodes", getTag().color().stream().map(Color::code).toList());
        }
        return tags;
    }
}
