package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.model.entity.AiComparisonFeedback;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.user.model.dto.UserDto;

public record AiComparisonDto(Long id,
                              String extractedImagePath1,
                              String extractedImagePath2,
                              UserDto userInfo,
                              boolean styleLiked,
                              int winner,
                              ImageType imageType
                             ) {
    public AiComparisonDto(AiComparisonFeedback feedback, String fileS3Url1, String fileS3Url2, UserDto userInfo) {
        this(feedback.getId(), fileS3Url1, fileS3Url2 ,userInfo, feedback.isLikeStyle(), feedback.getWinner(), feedback.getImageType());
    }
}
