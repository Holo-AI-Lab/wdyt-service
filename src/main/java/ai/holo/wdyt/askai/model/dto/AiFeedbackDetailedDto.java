package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.model.entity.AiFeedback;
import ai.holo.wdyt.askai.model.entity.ImageType;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.user.model.dto.UserDto;

public record AiFeedbackDetailedDto(Long id,
                                    OutfitAnalysis outfitAnalysis,
                                    HeadStyleAnalysis headStyleAnalysis,
                                    ImageType imageType,
                                    String extractedImagePath,
                                    UserDto userInfo,
                                    boolean styleLiked,
                                    Boolean aiResponseLiked,
                                    Integer topListOrder,
                                    Integer order,
                                    LocationAndWeatherDto locationAndWeather) {

    public AiFeedbackDetailedDto(AiFeedback feedback, OutfitAnalysis outfitAnalysis, HeadStyleAnalysis headStyleAnalysis,
                                 String extractedImagePath, UserDto userInfo) {
        this(feedback.getId(), outfitAnalysis, headStyleAnalysis, feedback.getImageType(), extractedImagePath, userInfo, feedback.isLikeStyle(),
                feedback.getLikeAiResponse(), feedback.getTopListOrder(), feedback.getOrder(), feedback.getLocationAndWeather());
    }
}