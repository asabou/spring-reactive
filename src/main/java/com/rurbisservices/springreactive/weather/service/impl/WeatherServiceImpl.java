package com.rurbisservices.springreactive.weather.service.impl;

import com.rurbisservices.springreactive.weather.service.abstracts.IWeatherService;
import com.rurbisservices.springreactive.weather.service.model.*;
import com.rurbisservices.springreactive.weather.utils.ServiceUtils;
import com.rurbisservices.springreactive.weather.utils.WeatherAppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class WeatherServiceImpl implements IWeatherService {
    @Autowired
    private WeatherAppProperties properties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient webClient;

    @Override
    public Flux<TemperatureDTO> getTemperatureV1(String cities) {
        if (ServiceUtils.isStringNullOrEmpty(cities)) {
            return Flux.empty();
        }
        Flux<String> citiesFlux = Flux.just(cities.split(","));
        return citiesFlux
                .delayElements(Duration.ofSeconds(1))
                .map(city -> {
                    CityTempFromApiDTO cityTemp = new CityTempFromApiDTO();
                    try {
                        cityTemp = restTemplate.getForObject(properties.getWeatherApi() + "/" + city, CityTempFromApiDTO.class);
                    } catch (Exception e) {
                        log.error("error when trying to retrieve data for {}", city);
                    }
                    AvgTempWindDTO avg = getAvgTempWindDTO(cityTemp.getForecast());
                    return new TemperatureDTO(city, avg.getTemperature(), avg.getWind());
                });
    }

    /*
    * flatMap vs map
    *
    * Map transforms the items emitted by an Observable by applying a function to each item.
    * FlatMap transforms the items emitted by an Observable into Observables.
    * So, the main difference between Map and FlatMap is that FlatMap mapper returns an observable itself,
    * so it is used to map over asynchronous operations.
    * */
    @Override
    public Flux<TemperatureDTO> getTemperatureV2(String cities) {
        if (ServiceUtils.isStringNullOrEmpty(cities)) {
            return Flux.empty();
        }
        Flux<String> citiesFlux = Flux.just(cities.split(","));
        return citiesFlux
                .delayElements(Duration.ofSeconds(1))
                .flatMap(city -> webClient.get().uri("/" + city)
                        .retrieve()
                        .bodyToMono(CityTempFromApiDTO.class)
                        .onErrorResume(e -> Mono.just(new CityTempFromApiDTO()))
                        .map(cityTempFromApi -> new CityTempDTO(city, cityTempFromApi.getTemperature(),
                                cityTempFromApi.getWind(), cityTempFromApi.getDescription(), cityTempFromApi.getForecast()))
                ).map(cityTemp -> {
                    AvgTempWindDTO avg = getAvgTempWindDTO(cityTemp.getForecast());
                    return new TemperatureDTO(cityTemp.getCity(), avg.getTemperature(), avg.getWind());
                })
                .doOnNext(temp -> System.out.println(temp));
    }

    private AvgTempWindDTO getAvgTempWindDTO(List<ForecastDTO> forecasts) {
        AvgTempWindDTO avg = new AvgTempWindDTO();
        if (!ServiceUtils.isListNullOrEmpty(forecasts)) {
            double temp = 0.0;
            int nrTemp = 0;
            double wind = 0.0;
            int nrWind = 0;
            for (ForecastDTO forecast : forecasts) {
                if (ServiceUtils.isStringNumber(forecast.getTemperature())) {
                    temp += ServiceUtils.convertStringToNumber(forecast.getTemperature());
                    nrTemp++;
                }
                if (ServiceUtils.isStringNumber(forecast.getWind())) {
                    wind += ServiceUtils.convertStringToNumber(forecast.getWind());
                    nrWind++;
                }
            }
            if (nrWind > 0) {
                avg.setWind(String.format("%.2f", wind / nrWind));
            }
            if (nrTemp > 0) {
                avg.setTemperature(String.format("%.2f", temp / nrTemp));
            }
        }
        return avg;
    }
}
