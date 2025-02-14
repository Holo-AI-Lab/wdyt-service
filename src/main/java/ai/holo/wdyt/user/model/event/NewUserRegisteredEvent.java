package ai.holo.wdyt.user.model.event;

import ai.holo.wdyt.common.event.Event;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Getter
public class NewUserRegisteredEvent extends Event {
    private final Long userId;

    public NewUserRegisteredEvent(Long userId) {
        this.userId = userId;
    }
    @Override
    protected List<Pair<Object, Object>> getEqualityCheckPairs(Object otherObj) {
        return List.of(new ImmutablePair<>(userId, ((NewUserRegisteredEvent) otherObj).userId));
    }

    @Override
    protected List<Object> getHashCodeContributors() {
        return List.of(userId);
    }
}
