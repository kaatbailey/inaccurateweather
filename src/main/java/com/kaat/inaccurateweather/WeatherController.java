package com.kaat.inaccurateweather;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.CookieValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WeatherController {

    @Autowired
    private JwtService jwtService;

    private final Environment env;
    private final RestTemplate restTemplate;

    private final WeatherService weatherService;

    public WeatherController(Environment env, RestTemplate restTemplate, WeatherService weatherService) {
        this.env = env;
        this.restTemplate = restTemplate;
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public String showWeather(Model model, @RequestParam(value = "zipCode", required = false) String zipCode,
                               @CookieValue(value = "jwt", required = false) String jwt,
                               HttpServletRequest request) {
        String ipAddress = "98.97.24.220";;//Generic place holder address if we can't get one.

        //if no zipCode we'll try to pick the last good search one up from jwt.
        if (zipCode == null) {
            if (jwt != null) {
                zipCode = jwtService.getZipCodeFromToken(jwt);
            }
            if (zipCode != null) {
                model.addAttribute("zipCode", zipCode);
            }

            //if we don't have a zip rom jwt we'll try to get one via ipaddress to geolocation.
            if (zipCode == null || zipCode.isEmpty()) {
                // Get the user's IP address from the request
              String requestAddress = request.getRemoteAddr();
                //If we're testing on localhost we won't pick up the RemoteAddr, keep the default.
                if (!requestAddress.equals("0:0:0:0:0:0:0:1")) {
                    ipAddress = requestAddress;
                }
            }
        }

        // Call GeolocateIPAddress method in WeatherService
        String apikey = env.getProperty("ipstack");
        zipCode = weatherService.GeolocateIPAddress(ipAddress, apikey);


        //save any zipCode we have. if we don't have a cookie zip or a geolocate zip we'll have to use a default.
        if (!zipCode.isEmpty() || zipCode != null){
            String token = jwtService.createToken(zipCode);
          }

        // Call IpaddressToLonLatConvert method in WeatherService
        OpenWeatherMapGeoResponse geoResponse = weatherService.IpaddressToLonLatConvert(zipCode, env.getProperty("openweathermap"));

        // Call GetWeatherFromLonLat method in WeatherService
        WeatherResponse response = weatherService.GetWeatherFromLonLat(geoResponse, zipCode, env.getProperty("openweathermap"));

        List<WeatherResponse.Weather> weather = response.getList();
// Extract specific data from the response
        double lon = response.getCoord().getLon();
        double lat = response.getCoord().getLat();
        int temp = response.getMain().getTemp();
        int feelsLike = response.getMain().getFeels_like();
        int pressure = response.getMain().getPressure();
        int humidity = response.getMain().getHumidity();
        int windSpeed = response.getWind().getSpeed();
        int windDirection = response.getWind().getDeg();
        int visibility = response.getVisibility();
        int cloudiness = response.getClouds().getAll();
        long dateTime = response.getDt();
        String name = response.getName();
        int cityId = response.getId();
        int responseCode = response.getCod();

// Extract weather condition data
        int conditionId = weather.get(0).getId();
        String conditionMain = weather.get(0).getMain();
        String conditionDescription = weather.get(0).getDescription();
        String conditionIconFilename = getIconFilename(conditionDescription);
        String iconUrl = "/img/" + conditionIconFilename;

// Add icon URL to the model
        model.addAttribute("iconUrl", iconUrl);



// Extract country data
        long sunriseTime = response.getSys().getSunrise();
        long sunsetTime = response.getSys().getSunset();

        model.addAttribute("weather", weather);
        model.addAttribute("zipCode", zipCode);
        model.addAttribute("lon", lon);
        model.addAttribute("lat", lat);
        model.addAttribute("temp", temp);
        model.addAttribute("feelsLike", feelsLike);
        //model.addAttribute("tempMin", tempMin);
        //model.addAttribute("tempMax", tempMax);
        model.addAttribute("pressure", pressure);
        model.addAttribute("humidity", humidity);
        model.addAttribute("windSpeed", windSpeed);
        model.addAttribute("windDirection", windDirection);
        model.addAttribute("visibility", visibility);
        model.addAttribute("cloudiness", cloudiness);
        model.addAttribute("dateTime", dateTime);
        model.addAttribute("name", name);
        model.addAttribute("cityId", cityId);
        model.addAttribute("responseCode", responseCode);
        model.addAttribute("conditionId", conditionId);
        model.addAttribute("conditionMain", conditionMain);
        model.addAttribute("conditionDescription", conditionDescription);
        model.addAttribute("conditionIcon", iconUrl);
        model.addAttribute("sunriseTime", sunriseTime);
        model.addAttribute("sunsetTime", sunsetTime);
        return "weather";



    }

    //this just handles all of the images that go with the weather.
    private String getIconFilename(String description) {
        String filename = "";
        switch (description) {
            case "clear sky":
                filename = "clear-day.svg";
                break;
            case "few clouds":
                filename = "cloudy-1-day.svg";
                break;
            case "scattered clouds":
            case "broken clouds":
                filename = "cloudy-2-day.svg";
                break;
            case "shower rain":
            case "rain":
                filename = "rainy-1.svg";
                break;
            case "thunderstorm":
                filename = "scattered-thunderstorms.svg";
                break;
            case "snow":
            case "light snow":
            case "heavy snow":
                filename = "snowy-1.svg";
                break;
            case "mist":
                filename = "rainy-3.svg";
                break;
            case "overcast clouds":
                filename = "cloudy-3-day.svg";
                break;
        }
        return filename;
    }//what all weather conditions am i missing set a default?

}

