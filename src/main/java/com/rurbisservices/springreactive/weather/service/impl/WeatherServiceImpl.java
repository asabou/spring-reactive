package com.rurbisservices.springreactive.weather.service.impl;

import com.rurbisservices.springreactive.weather.service.abstracts.IWeatherService;
import com.rurbisservices.springreactive.weather.service.model.CityTempDTO;
import com.rurbisservices.springreactive.weather.service.model.CityTempFromApiDTO;
import com.rurbisservices.springreactive.weather.service.model.ForecastDTO;
import com.rurbisservices.springreactive.weather.service.model.TemperatureDTO;
import com.rurbisservices.springreactive.weather.utils.ServiceUtils;
import com.rurbisservices.springreactive.weather.utils.WeatherAppProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WeatherServiceImpl implements IWeatherService {

    private final WeatherAppProperties properties;
    private final WebClient webClient;
    /*
    * constructor injection is recommended over field injection, and has several advantages:
        * the dependencies are clearly identified. There is no way to forget one when testing, or instantiating the object
             in any other circumstance (like creating the bean instance explicitly in a config class)
        * the dependencies can be final, which helps with robustness and thread-safety
        * you don't need reflection to set the dependencies. InjectMocks is still usable, but not necessary.
            You can just create mocks by yourself and inject them by simply calling the constructor
    * */
    public WeatherServiceImpl(WeatherAppProperties properties,
                              WebClient webClient) {
        this.properties = properties;
        this.webClient = webClient;
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
    public Flux<TemperatureDTO> getTemperature(String cities) {
        final CSVPrinter printer = initCSVPrinter();
        if (ServiceUtils.isStringNullOrEmpty(cities)) {
            return Flux.empty();
        }
        Flux<String> citiesFlux = Flux.fromIterable(Arrays.stream(cities.split(",")).sorted().collect(Collectors.toList()));
        return citiesFlux
                .delayElements(Duration.ofSeconds(1))
                .flatMap(city -> getInfoFromWeatherServer(city))
                .map(cityTemp -> getAvgTempWindDTO(cityTemp))
                .doOnNext(temp -> log.info("City: {}", temp.getName()))
                .doOnNext(temp -> appendToCSV(printer, temp))
                .doFinally(signal -> {
                    try {
                        printer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

    }

    public Mono<CityTempDTO> getInfoFromWeatherServer(String city) {
        return webClient.get().uri("/" + city)
                .retrieve()
                .bodyToMono(CityTempFromApiDTO.class)
                .onErrorResume(e -> Mono.just(new CityTempFromApiDTO()))
                .map(cityTempFromApi -> new CityTempDTO(city, cityTempFromApi.getTemperature(), cityTempFromApi.getWind(),
                        cityTempFromApi.getDescription(), cityTempFromApi.getForecast()));
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
            throw new RuntimeException("Append to CSV failed. " + e.getMessage());
        }
    }

    private TemperatureDTO getAvgTempWindDTO(CityTempDTO cityTemp) {
        TemperatureDTO avg = new TemperatureDTO();
        avg.setName(cityTemp.getCity());
        if (!ServiceUtils.isListNullOrEmpty(cityTemp.getForecast())) {
            double temp = 0.0;
            int nrTemp = 0;
            double wind = 0.0;
            int nrWind = 0;
            for (ForecastDTO forecast : cityTemp.getForecast()) {
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
