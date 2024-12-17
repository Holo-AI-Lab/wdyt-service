package ai.holo.wdyt.user.model.entity;

public enum Gender {

    FEMALE(0),
    MALE(1);

    private final int genderCode;

    Gender(int genderCode) {
        this.genderCode = genderCode;
    }

    public int getGenderCode() {
        return genderCode;
    }
}
