package ai.holo.wdyt.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(value = {"eventName", "fallback"}, ignoreUnknown = true)
public abstract class Event {
    private UUID eventId;
    private boolean fallback;

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    public String getEventName() {
        return getClass().getName();
    }

    public Boolean consistencyMechanismEnabled() {
        return true;
    }

    public final boolean equals(Object obj) {
        if (!(this.getClass().isInstance(obj))) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        return getEqualityCheckPairs(obj).stream().reduce(new EqualsBuilder(),
                (builder, pair) -> builder.append(pair.getLeft(), pair.getRight()), (b1,b2) -> b1).isEquals();
    }

    protected abstract List<Pair<Object, Object>> getEqualityCheckPairs(Object otherObj);

    public final int hashCode() {
        return getHashCodeContributors().stream().reduce(new HashCodeBuilder(), HashCodeBuilder::append,
                (b1, b2) -> b1).hashCode();
    }

    protected abstract List<Object> getHashCodeContributors();
}
