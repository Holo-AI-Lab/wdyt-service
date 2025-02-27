package ai.holo.wdyt.askai.model.entity;

import lombok.Getter;

import java.util.Objects;

@Getter
public class PromptKey {
    private final ImageType imageType;
    private final SubmissionType submissionType;

    public PromptKey(ImageType imageType, SubmissionType submissionType){
        this.submissionType = submissionType;
        this.imageType = imageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromptKey)) return false;
        PromptKey that = (PromptKey) o;
        return imageType == that.imageType && submissionType == that.submissionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageType, submissionType);
    }
}
