package ai.holo.wdyt.user.model.entity;

import ai.holo.wdyt.common.json.JsonConverter;
import ai.holo.wdyt.user.model.dto.UserSelectedStyle;

public class UserSelectedStyleConverter extends JsonConverter<UserSelectedStyle> {

    public UserSelectedStyleConverter() {
        super(UserSelectedStyle.class);
    }
}
