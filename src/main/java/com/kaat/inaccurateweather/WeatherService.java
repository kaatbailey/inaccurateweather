package com.kaat.inaccurateweather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    private final Environment env;
    private final RestTemplate restTemplate;
    // Create logger instance
    private static final Logger logger = Logger.getLogger(String.valueOf(WeatherController.class));


    @Autowired
    public WeatherService(Environment env, RestTemplate restTemplate) {
        this.env = env;
        this.restTemplate = restTemplate;
    }


    // Use ipstack API to get user's location using the user's IP address, but not localhost.
    public String GeolocateIPAddress(String ipaddress, String apikey) {
        logger.info("GeolocateIPAddress called with ipaddress={} " + ipaddress);
        String ipstackUrl = "http://api.ipstack.com/" + ipaddress + "?access_key=" + env.getProperty("ipstack");
        IpstackResponse ipstackResponse = restTemplate.getForObject(ipstackUrl, IpstackResponse.class);
       String zipCode = ipstackResponse.getZip();
       //MONTHLY USAGE LIMIT REACHED TEMP CODE
        if(zipCode == null){zipCode = "45697";} //THIS IS TEMPORARY BECAUSE WE USE THE FREE VERSION OF IPSTACK AND OUR LIMIT IS IN EFFECT
       if (zipCode!=null && !zipCode.isEmpty()){
           logger.info("GeolocateIPAddress returned zipCode={}" + zipCode);
           return zipCode;
       } else {
           logger.info("GeolocateIPAddress returned empty zipCode");
           return "notfound";}
    }

    //OpenWeather only takes lon and lat for weather resolution instead of ipaddress, so resolve
    public OpenWeatherMapGeoResponse IpaddressToLonLatConvert(String zipCode, String apikey){
        // Use OpenWeatherMap Geo API to get latitude and longitude from zip code
        logger.info("IpaddressToLonLatConvert called with zipCode={}" + zipCode);

        String geoUrl = "https://api.openweathermap.org/geo/1.0/zip?zip={zipCode},US&appid={apiKey}";
        Map<String, String> geoParams = new HashMap<>();
        geoParams.put("zipCode", zipCode); //CHANGE THIS BACK TO zipCode.
        geoParams.put("apiKey", env.getProperty("openweathermap"));

        OpenWeatherMapGeoResponse geoResponse = restTemplate.getForObject(geoUrl, OpenWeatherMapGeoResponse.class, geoParams);
        //if geoResponse not empty then we'll return it.
        logger.info("IpaddressToLonLatConvert returned geoResponse={} " + geoResponse);

        return geoResponse;

    }

    //pass in lon and lat and get day of weather
    public WeatherResponse GetWeatherFromLonLat(OpenWeatherMapGeoResponse geoResponse, String zipCode, String apikey){
        logger.info("GetWeatherFromLonLat called with geoResponse={}, and zipCode={} " + geoResponse + " " + zipCode);

        String openWeatherMapUrl = "https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={apiKey}&units=imperial";
        // FOR A 5 DAY WE WOULD USE THE WORD FORCAST INSTEAD OF weather?lat etc.
        Map<String, String> forecastParams = new HashMap<>();
        forecastParams.put("lat", String.valueOf(geoResponse.getLat()));
        forecastParams.put("lon", String.valueOf(geoResponse.getLon()));
        forecastParams.put("apiKey", env.getProperty("openweathermap"));
        WeatherResponse response = restTemplate.getForObject(openWeatherMapUrl, WeatherResponse.class, forecastParams);
        logger.info("GetWeatherFromLonLat returned response={} " + response);
        return response;
    }




}
