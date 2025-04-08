package ai.holo.wdyt.askai.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Taggable {
    String OCCASION = "occasion";
    String STYLE = "style";
    String COLOR = "color";
    String COLOR_CODE = "colorCode";

    Tag getTag();

    @JsonIgnore
    default Map<String, List<String>> getTags() {
        Map<String, List<String>> tags = new HashMap<>();
        if (!CollectionUtils.isEmpty(getTag().style())) {
            tags.put(STYLE, getTag().style());
        }
        if (!CollectionUtils.isEmpty(getTag().occasion())) {
            tags.put(OCCASION, getTag().occasion());
        }
        if (!CollectionUtils.isEmpty(getTag().color())) {
            tags.put(COLOR, getTag().color().stream().map(Color::name).toList());
        }
        if (!CollectionUtils.isEmpty(getTag().color())) {
            tags.put(COLOR_CODE, getTag().color().stream().map(Color::code).toList());
        }
        return tags;
    }
}
