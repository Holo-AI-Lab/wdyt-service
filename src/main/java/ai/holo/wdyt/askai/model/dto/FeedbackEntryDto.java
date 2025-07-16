package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.model.entity.FeedbackEntry;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;
import ai.holo.wdyt.user.model.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FeedbackEntryDto(String id,
                               UserDto aiUserInfo,
                               OutfitAnalysis outfitAnalysis,
                               ComparisonAnalysis comparisonAnalysis,
                               LocationAndWeatherDto locationAndWeather) {

    public FeedbackEntryDto(FeedbackEntry feedback, OutfitAnalysis outfitAnalysis, ComparisonAnalysis comparisonAnalysis, UserDto userInfo) {
        this(feedback.id(), userInfo, outfitAnalysis, comparisonAnalysis,  feedback.locationAndWeather());
    }
}