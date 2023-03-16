package com.kaat.inaccurateweather;

import lombok.Data;

@Data
public class OpenWeatherMapGeoResponse {
    private double lon;
    private double lat;
    private String name;
    private String state;
    private String country;


}
