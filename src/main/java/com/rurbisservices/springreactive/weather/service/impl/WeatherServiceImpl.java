package com.rurbisservices.springreactive.weather.service.impl;

import com.rurbisservices.springreactive.weather.service.abstracts.IWeatherService;
import com.rurbisservices.springreactive.weather.service.model.*;
import com.rurbisservices.springreactive.weather.utils.ServiceUtils;
import com.rurbisservices.springreactive.weather.utils.WeatherAppProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        final CSVPrinter printer = initCSVPrinter();
        if (ServiceUtils.isStringNullOrEmpty(cities)) {
            return Flux.empty();
        }
        //TODO: Sorting the cities at the beginning will not guarantee the sorted Flux (I think)
        Flux<String> citiesFlux = Flux.fromIterable(Arrays.stream(cities.split(",")).sorted().collect(Collectors.toList()));
        return citiesFlux
                .delayElements(Duration.ofSeconds(1))
                .map(city -> {
                    CityTempFromApiDTO cityTemp = new CityTempFromApiDTO();
                    try {
                        //sync request (not expected, but it's working)
                        cityTemp = restTemplate.getForObject(properties.getWeatherApi() + "/" + city, CityTempFromApiDTO.class);
                    } catch (Exception e) {
                        log.error("error when trying to retrieve data for {}", city);
                    }
                    log.info("City: {}", city);
                    AvgTempWindDTO avg = getAvgTempWindDTO(cityTemp.getForecast());
                    return new TemperatureDTO(city, avg.getTemperature(), avg.getWind());
                }).doOnNext(temp -> appendToCSV(printer, temp));
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
        final CSVPrinter printer = initCSVPrinter();
        if (ServiceUtils.isStringNullOrEmpty(cities)) {
            return Flux.empty();
        }
        //TODO: Sorting the cities at the beginning will not guarantee the sorted Flux (I think)
        Flux<String> citiesFlux = Flux.fromIterable(Arrays.stream(cities.split(",")).sorted().collect(Collectors.toList()));
        return citiesFlux
                .delayElements(Duration.ofSeconds(1))
                .flatMap(city -> webClient.get().uri("/" + city)
                        .retrieve()
                        .bodyToMono(CityTempFromApiDTO.class)
                        .onErrorResume(e -> Mono.just(new CityTempFromApiDTO()))
                        .map(cityTempFromApi -> new CityTempDTO(city, cityTempFromApi.getTemperature(),
                                cityTempFromApi.getWind(), cityTempFromApi.getDescription(), cityTempFromApi.getForecast())
                        )
                ).map(cityTemp -> {
                    log.info("City: {}", cityTemp.getCity());
                    AvgTempWindDTO avg = getAvgTempWindDTO(cityTemp.getForecast());
                    return new TemperatureDTO(cityTemp.getCity(), avg.getTemperature(), avg.getWind());
                })
                .doOnNext(temp -> appendToCSV(printer, temp));
    }

    private CSVPrinter initCSVPrinter() {
        try {
            final BufferedWriter writer = Files.newBufferedWriter(Paths.get(properties.getWeatherCSVFile()));
            final CSVFormat csvFormat = CSVFormat.Builder.create().build().builder()
                    .setHeader(properties.getWeatherCSVHeader())
                    .setDelimiter(properties.getWeatherCSVSeparator())
                    .build();
            return new CSVPrinter(writer, csvFormat);
        } catch (Exception e) {
            throw new RuntimeException("CSV Printer initialization failed!");
        }
    }

    private void appendToCSV(CSVPrinter printer, TemperatureDTO temperature) {
        try {
            printer.printRecord(temperature.getName(), temperature.getTemperature(), temperature.getWind());
            printer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Append to CSV failed");
        }
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
