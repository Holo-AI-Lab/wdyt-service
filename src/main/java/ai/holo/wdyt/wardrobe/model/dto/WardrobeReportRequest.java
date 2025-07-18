package ai.holo.wdyt.wardrobe.model.dto;

public record WardrobeReportRequest(Long itemId, String feedback) {
    public WardrobeReportRequest {
        if (feedback == null || feedback.isBlank()) {
            throw new IllegalArgumentException("Feedback must not be empty.");
        }
    }
}
