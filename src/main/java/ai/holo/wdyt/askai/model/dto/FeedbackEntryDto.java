package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.model.entity.FeedbackEntry;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.user.model.dto.UserDto;

public record FeedbackEntryDto(String id,
                               UserDto aiUserInfo,
                               OutfitAnalysis outfitAnalysis,
                               HeadStyleAnalysis headStyleAnalysis,
                               Boolean aiResponseLiked,
                               LocationAndWeatherDto locationAndWeather) {

    public FeedbackEntryDto(FeedbackEntry feedback, OutfitAnalysis outfitAnalysis, HeadStyleAnalysis headStyleAnalysis, UserDto userInfo) {
        this(feedback.id(), userInfo, outfitAnalysis, headStyleAnalysis, feedback.likeAiResponse(), feedback.locationAndWeather());
    }
}