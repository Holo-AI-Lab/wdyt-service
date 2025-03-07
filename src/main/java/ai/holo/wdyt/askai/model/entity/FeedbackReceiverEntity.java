package ai.holo.wdyt.askai.model.entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FeedbackReceiverEntity {
    List<FeedbackEntry> getFeedbackEntries();

    default void addFeedbackEntry(FeedbackEntry feedbackEntry) {
        getFeedbackEntries().add(feedbackEntry);
    }

    default void removeFeedbackEntry(FeedbackEntry feedbackEntry) {
        getFeedbackEntries().remove(feedbackEntry);
    }
}
