package ai.holo.wdyt.user.model.entity;

public enum Gender {
    FEMALE("female"),
    MALE("male"),
    NON_BINARY("non-binary"),
    ;

    private final String name;

    Gender(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
