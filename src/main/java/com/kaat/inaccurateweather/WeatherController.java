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
import java.util.logging.Logger;

@Controller
public class WeatherController {

    @Autowired
    private JwtService jwtService;

    private final Environment env;
    private final RestTemplate restTemplate;

    private final WeatherService weatherService;

    // Create logger instance
    private static final Logger logger = Logger.getLogger(String.valueOf(WeatherController.class));


    public WeatherController(Environment env, RestTemplate restTemplate, WeatherService weatherService) {
        this.env = env;
        this.restTemplate = restTemplate;
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public String showWeather(Model model, @RequestParam(value = "zipCode", required = false) String zipCode,
                               @CookieValue(value = "jwt", required = false) String jwt,
                               HttpServletRequest request) {
        // Log request parameters
        logger.info("Received request for weather with zipCode=" + zipCode + " and jwt=" + jwt);

        String ipAddress = "98.97.24.220";;//Generic place holder address if we can't get one.

        //if no zipCode we'll try to pick the last good search one up from jwt.
        if (zipCode == null) {
            if (jwt != null) {
                zipCode = jwtService.getZipCodeFromToken(jwt);
            }
            if (zipCode != null) {
                model.addAttribute("zipCode", zipCode);
            }

            // Log zipCode retrieval from jwt or request parameters
            logger.info("Retrieved zipCode=" + zipCode + " from jwt or request parameters");


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
        // Log zipCode retrieval from ipaddress geolocation
        logger.info("Retrieved zipCode=" + zipCode + " from ipaddress geolocation");

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

    private static final Map<String, String> iconFilenames = new HashMap<>();
    static {
            iconFilenames.put("clear sky", "clear-day.svg");
            iconFilenames.put("few clouds", "cloudy-1-day.svg");
            iconFilenames.put("scattered clouds", "cloudy-2-day.svg");
            iconFilenames.put("broken clouds", "cloudy-2-day.svg");
            iconFilenames.put("shower rain", "rainy-1.svg");
            iconFilenames.put("rain", "rainy-1.svg");
            iconFilenames.put("thunderstorm", "scattered-thunderstorms.svg");
            iconFilenames.put("snow", "snowy-1.svg");
            iconFilenames.put("light snow", "snowy-1.svg");
            iconFilenames.put("heavy snow", "snowy-1.svg");
            iconFilenames.put("mist", "rainy-3.svg");
            iconFilenames.put("overcast clouds", "cloudy-3-day.svg");
            iconFilenames.put("moderate rain", "rainy-1.svg");
            iconFilenames.put("light rain", "rainy-1.svg");
            iconFilenames.put("haze", "haze-day.svg");
            iconFilenames.put("fog", "fog-day.svg");
            iconFilenames.put("smoke", "rainy-3.svg");
            iconFilenames.put("dust", "rainy-3.svg");
            iconFilenames.put("sand", "rainy-3.svg");
            iconFilenames.put("ash", "rainy-3.svg");
            iconFilenames.put("squall", "rainy-1.svg");
            iconFilenames.put("tornado", "scattered-thunderstorms.svg");
            iconFilenames.put("volcanic ash", "haze-day.svg");
            iconFilenames.put("drizzle", "rainy-1.svg");
            iconFilenames.put("freezing rain", "snow-and-sleet-mix.svg");
            iconFilenames.put("heavy intensity rain", "rainy-1.svg");
            iconFilenames.put("very heavy rain", "rainy-1.svg");
            iconFilenames.put("extreme rain", "hurricane.svg");
            iconFilenames.put("heavy intensity shower rain", "rainy-1.svg");
            iconFilenames.put("ragged shower rain", "rainy-1.svg");
            iconFilenames.put("light intensity drizzle", "rainy-1.svg");
            iconFilenames.put("drizzle rain", "rainy-1.svg");
            iconFilenames.put("heavy intensity drizzle", "rainy-1.svg");
            iconFilenames.put("light intensity shower rain", "rainy-1.svg");
            iconFilenames.put("shower snow", "snowy-1.svg");
            iconFilenames.put("light shower snow", "snowy-1.svg");
            iconFilenames.put("heavy shower snow", "snowy-1.svg");
            iconFilenames.put("freezing drizzle", "snowy-1.svg");
            iconFilenames.put("light rain and snow", "snowy-1.svg");
            iconFilenames.put("rain and snow", "snowy-1.svg");
            iconFilenames.put("light shower sleet", "snowy-1.svg");
            iconFilenames.put("shower sleet", "snowy-1.svg");
            iconFilenames.put("light rain and snow","snowy-1.svg");
            iconFilenames.put("rain and snow","snowy-1.svg");
            iconFilenames.put("light shower snow","snowy-1.svg");
            iconFilenames.put("shower snow","snowy-1.svg");
            iconFilenames.put("heavy shower snow","snowy-1.svg");
            iconFilenames.put("smoke","fog.svg");
            iconFilenames.put("haze","fog.svg");
            iconFilenames.put("sand", "dust.svg");
            iconFilenames.put("fog","fog.svg");
            iconFilenames.put("sand","fog.svg");
            iconFilenames.put("dust","dust.svg");
            iconFilenames.put("volcanic ash","fog.svg");
        }

    private String getIconFilename(String description) {
        return iconFilenames.getOrDefault(description, "tornado.svg");
    }

}

