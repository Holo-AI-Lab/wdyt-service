package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.model.entity.AiComparisonFeedback;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.user.model.dto.UserDto;

import java.util.List;

public record AiComparisonDetailedDto(Long id,
                                      ImageType imageType,
                                      String imagePath1,
                                      String imagePath2,
                                      UserDto userInfo,
                                      List<FeedbackEntryDto> feedbackEntries) {

    public AiComparisonDetailedDto(AiComparisonFeedback comparisonFeedback, String imagePath1, String imagePath2,
                                   UserDto userInfo, List<FeedbackEntryDto> feedbackEntryDtos) {
        this(comparisonFeedback.getId(), comparisonFeedback.getImageType(), imagePath1, imagePath2, userInfo, feedbackEntryDtos);
    }
}