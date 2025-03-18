package ai.holo.wdyt.askai.model.entity;

import java.util.List;

public interface FeedbackReceiverEntity {
    List<FeedbackEntry> getFeedbackEntries();

    default void addFeedbackEntry(FeedbackEntry feedbackEntry) {
        getFeedbackEntries().add(feedbackEntry);
    }

    default void removeFeedbackEntry(FeedbackEntry feedbackEntry) {
        getFeedbackEntries().remove(feedbackEntry);
    }
}
