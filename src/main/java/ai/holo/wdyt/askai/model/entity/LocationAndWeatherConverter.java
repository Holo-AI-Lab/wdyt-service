package ai.holo.wdyt.askai.model.entity;

import ai.holo.wdyt.common.json.JsonConverter;
import ai.holo.wdyt.location.model.LocationAndWeatherDto;

public class LocationAndWeatherConverter extends JsonConverter<LocationAndWeatherDto> {

    public LocationAndWeatherConverter() {
        super(LocationAndWeatherDto.class);
    }
}
