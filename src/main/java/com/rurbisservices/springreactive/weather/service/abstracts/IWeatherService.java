package com.rurbisservices.springreactive.weather.service.abstracts;

import com.rurbisservices.springreactive.weather.service.model.TemperatureDTO;
import reactor.core.publisher.Flux;

public interface IWeatherService {
    Flux<TemperatureDTO> getTemperatureV1(String cities);
    Flux<TemperatureDTO> getTemperatureV2(String cities);

}
