package com.kaat.inaccurateweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import(AppConfig.class)
public class InAccurateWeatherApplication {

    public static void main(String[] args) {
        SpringApplication.run(InAccurateWeatherApplication.class, args);
    }

}
