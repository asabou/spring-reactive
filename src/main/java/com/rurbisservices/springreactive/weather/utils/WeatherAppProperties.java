package com.rurbisservices.springreactive.weather.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class WeatherAppProperties {
    @Value("${weather.api}")
    private String weatherApi;
}
