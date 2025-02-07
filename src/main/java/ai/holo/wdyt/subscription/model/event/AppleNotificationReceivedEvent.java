package ai.holo.wdyt.subscription.model.event;

import ai.holo.wdyt.common.event.Event;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Getter
public class AppleNotificationReceivedEvent extends Event {
    private final Long appleNotificationId;

    public AppleNotificationReceivedEvent(Long appleNotificationId) {
        this.appleNotificationId = appleNotificationId;
    }
    @Override
    protected List<Pair<Object, Object>> getEqualityCheckPairs(Object otherObj) {
        return List.of(new ImmutablePair<>(appleNotificationId, ((AppleNotificationReceivedEvent) otherObj).appleNotificationId));
    }

    @Override
    protected List<Object> getHashCodeContributors() {
        return List.of(appleNotificationId);
    }
}
