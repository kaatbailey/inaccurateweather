package com.kaat.inaccurateweather;

import lombok.Data;

import java.util.List;

@Data
public class WeatherResponse {
    private Coord coord;
    private List<Weather> weather;
    private String base;
    private MainData main;
    private int visibility;
    private Wind wind;
    private Clouds clouds;
    private long dt;
    private SysData sys;
    private int timezone;
    private int id;
    private String name;
    private int cod;


    public List<Weather> getList() {
        return weather;
    }

@Data
    public class Coord {
        private double lon;
        private double lat;
    }

    @Data
    public static class Weather {
        private int id;
        private String main;
        private String description;
        private String icon;
        private String iconUrl;


    }

    @Data
    public class MainData {
        private int temp;
        private int feels_like;
        private double temp_min;
        private double temp_max;
        private int pressure;
        private int humidity;
        private int sea_level;
        private int grnd_level;


    }

    @Data
    public class Wind {
        private int speed;
        private int deg;
        private double gust;

    }

    @Data
    public class Clouds {
        private int all;

    }

    @Data
    public class SysData {
        private String country;
        private long sunrise;
        private long sunset;

    }
}
