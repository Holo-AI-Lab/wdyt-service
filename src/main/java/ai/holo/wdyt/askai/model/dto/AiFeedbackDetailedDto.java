package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.user.model.dto.UserDto;

public record AiFeedbackDetailedDto(Long id,
                                    OutfitAnalysis outfitAnalysis,
                                    HeadStyleAnalysis headStyleAnalysis,
                                    String extractedImagePath,
                                    UserDto userInfo,
                                    boolean styleLiked,
                                    Boolean aiResponseLiked,
                                    Integer topListOrder,
                                    Integer order,
                                    String location) {

    public AiFeedbackDetailedDto(AiFeedback feedback, OutfitAnalysis outfitAnalysis, HeadStyleAnalysis headStyleAnalysis,
                                 String extractedImagePath, UserDto userInfo) {
        this(feedback.getId(), outfitAnalysis, headStyleAnalysis, extractedImagePath, userInfo, feedback.isLikeStyle(),
                feedback.getLikeAiResponse(), feedback.getTopListOrder(), feedback.getOrder(), feedback.getLocation());
    }
}