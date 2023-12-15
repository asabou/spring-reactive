package com.rurbisservices.springreactive.weather.service.abstracts;

import com.rurbisservices.springreactive.weather.service.model.TemperatureDTO;
import reactor.core.publisher.Flux;

public interface IWeatherService {
    Flux<TemperatureDTO> getTemperature(String cities);
}
