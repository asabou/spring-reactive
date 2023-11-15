package com.rurbisservices.springreactive.weather.configs;

import com.rurbisservices.springreactive.weather.utils.WeatherAppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientBean {
    @Autowired
    private WeatherAppProperties properties;
    @Bean
    private WebClient webClient() {
        return WebClient.create(properties.getWeatherApi());
    }
}
