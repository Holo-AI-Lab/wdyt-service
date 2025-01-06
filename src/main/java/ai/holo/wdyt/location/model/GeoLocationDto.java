package ai.holo.wdyt.location.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record GeoLocationDto(String city, String country, String latitude, String longitude) {

    @JsonIgnore
    public String getLocation() {
        return isUnknown() ? "Unknown" : String.format("%s/%s", city(), country());
    }

    @JsonIgnore
    public boolean isUnknown() {
        return "Unknown".equals(city()) && "Unknown".equals(country());
    }

    @JsonIgnore
    public String commaSeperatedLatLong() {
        return String.format("%s,%s", latitude(), longitude());
    }
}
