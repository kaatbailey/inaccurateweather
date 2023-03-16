# inaccurateweather
This is a spring mvc weather application

This is a Java code for a web application that displays weather data for a given zip code. It uses Spring Framework and makes RESTful API calls to fetch data from OpenWeatherMap API and ipstack API.

The main class is WeatherController which is annotated with @Controller. It has a showWeather method that handles the GET requests for the URL "/weather". This method takes in Model object, HttpServletRequest object, and a @RequestParam annotated parameter for "zipCode" and a @CookieValue annotated parameter for "jwt" cookie. It uses these parameters to determine the location for which weather data is to be fetched.

It then calls methods from the WeatherService class to fetch the weather data by making RESTful API calls to OpenWeatherMap API and ipstack API. It extracts the necessary data from the response and adds it to the Model object. Finally, it returns the name of the HTML template ("weather") that should be rendered to display the weather data.

The getIconFilename method is a helper method that takes in a String describing the weather condition and returns the filename of the corresponding weather icon.
