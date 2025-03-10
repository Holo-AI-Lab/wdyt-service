package ai.holo.wdyt.askai.model.event;

import ai.holo.wdyt.common.event.Event;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Getter
public class AiFeedbackReceivedEvent extends Event {
    private final Long aiFeedbackId;
    private final Long feedbackReceiverUserId;
    private final Long feedbackGiverUserId;


    public AiFeedbackReceivedEvent(Long aiFeedbackId, Long feedbackReceiverUserId, Long feedbackGiverUserId) {
        this.aiFeedbackId = aiFeedbackId;
        this.feedbackReceiverUserId = feedbackReceiverUserId;
        this.feedbackGiverUserId = feedbackGiverUserId;
    }

    @Override
    protected List<Pair<Object, Object>> getEqualityCheckPairs(Object otherObj) {
        return List.of(new ImmutablePair<>(aiFeedbackId, ((AiFeedbackReceivedEvent) otherObj).aiFeedbackId));
    }

    @Override
    protected List<Object> getHashCodeContributors() {
        return List.of(aiFeedbackId);
    }
}
