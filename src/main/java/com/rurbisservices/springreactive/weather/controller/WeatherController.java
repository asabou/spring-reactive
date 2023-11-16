package com.rurbisservices.springreactive.weather.controller;

import com.rurbisservices.springreactive.weather.service.impl.WeatherServiceImpl;
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
    @Autowired
    private WeatherServiceImpl weatherService;

    @GetMapping("/v1/weather")
    public Flux<TemperatureDTO> getV1(@RequestParam(name = "city") String city) {
        return weatherService.getTemperatureV1(city);
    }

    @GetMapping("/v2/weather")
    public Flux<TemperatureDTO> getV2(@RequestParam(name = "city") String city) {
        return weatherService.getTemperatureV2(city);
    }

    @GetMapping("/weather")
    public Flux<TemperatureDTO> get(@RequestParam(name = "city") String city) {
        return getV2(city);
    }
}
