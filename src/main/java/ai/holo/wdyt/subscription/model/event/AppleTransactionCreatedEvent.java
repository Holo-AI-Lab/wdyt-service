package ai.holo.wdyt.subscription.model.event;

import ai.holo.wdyt.common.event.Event;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Getter
public class AppleTransactionCreatedEvent extends Event {
    private final Long appleTransactionId;

    public AppleTransactionCreatedEvent(Long appleTransactionId) {
        this.appleTransactionId = appleTransactionId;
    }
    @Override
    protected List<Pair<Object, Object>> getEqualityCheckPairs(Object otherObj) {
        return List.of(new ImmutablePair<>(appleTransactionId, ((AppleTransactionCreatedEvent) otherObj).appleTransactionId));
    }

    @Override
    protected List<Object> getHashCodeContributors() {
        return List.of(appleTransactionId);
    }
}
