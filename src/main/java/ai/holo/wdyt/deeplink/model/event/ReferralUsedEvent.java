package ai.holo.wdyt.deeplink.model.event;

import ai.holo.wdyt.common.event.Event;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Getter
public class ReferralUsedEvent extends Event {
    private final Long referringUserId;
    private final Long invitedUserId;

    public ReferralUsedEvent(Long referringUserId, Long invitedUserId) {
        this.referringUserId = referringUserId;
        this.invitedUserId = invitedUserId;
    }

    @Override
    protected List<Pair<Object, Object>> getEqualityCheckPairs(Object otherObj) {
        return List.of(new ImmutablePair<>(referringUserId, ((ReferralUsedEvent) otherObj).referringUserId),
                new ImmutablePair<>(invitedUserId, ((ReferralUsedEvent) otherObj).invitedUserId));
    }

    @Override
    protected List<Object> getHashCodeContributors() {
        return List.of(referringUserId, invitedUserId);
    }
}
