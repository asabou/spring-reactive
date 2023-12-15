package com.rurbisservices.springreactive.weather.controller;

import com.rurbisservices.springreactive.weather.service.abstracts.IWeatherService;
import com.rurbisservices.springreactive.weather.service.model.TemperatureDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class WeatherController {
    final IWeatherService weatherService;
    public WeatherController(IWeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public Flux<TemperatureDTO> get(@RequestParam(name = "city") String city) {
        return weatherService.getTemperature(city);
    }
}
