package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.user.model.dto.UserDto;

public record AiFeedbackDto(Long id,
                            String extractedImagePath,
                            UserDto userInfo,
                            boolean styleLiked,
                            ImageType imageType,
                            Integer topListOrder,
                            Integer order) {

    public AiFeedbackDto(AiFeedback feedback, String extractedImagePath, UserDto userInfo) {
        this(feedback.getId(), extractedImagePath, userInfo, feedback.isLikeStyle(),
                feedback.getImageType(), feedback.getTopListOrder(), feedback.getOrder());
    }
}