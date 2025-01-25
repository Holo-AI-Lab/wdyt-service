package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.user.model.dto.UserDto;

import java.util.List;

public record AiFeedbackDetailedDto(Long id,
                                    ImageType imageType,
                                    String extractedImagePath,
                                    UserDto userInfo,
                                    boolean styleLiked,
                                    Integer topListOrder,
                                    Integer order,
                                    List<FeedbackEntryDto> feedbackEntries) {

    public AiFeedbackDetailedDto(AiFeedback feedback, String extractedImagePath, UserDto userInfo, List<FeedbackEntryDto> feedbackEntries) {
        this(feedback.getId(), feedback.getImageType(), extractedImagePath, userInfo, feedback.isLikeStyle(),
                feedback.getTopListOrder(), feedback.getOrder(), feedbackEntries);
    }
}