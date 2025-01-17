package ai.holo.wdyt.user.enums;

import lombok.Getter;

@Getter
public enum FriendEnum {
    PENDING("PENDING"),
    FRIEND("FRIEND"),
    REJECTED("REJECTED");

    private final String status;

    FriendEnum(String status) {
        this.status = status;
    }

}
