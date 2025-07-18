package ai.holo.wdyt.wardrobe.model.entity;
import ai.holo.wdyt.common.json.JsonConverter;

public class WardrobeTagConverter extends JsonConverter<Tags> {

    public WardrobeTagConverter() {
        super(Tags.class);
    }
}