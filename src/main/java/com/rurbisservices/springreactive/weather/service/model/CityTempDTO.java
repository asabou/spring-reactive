package com.rurbisservices.springreactive.weather.service.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static com.rurbisservices.springreactive.weather.utils.Constants.EMPTY_STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CityTempDTO extends CityTempFromApiDTO {
    private String city = EMPTY_STRING;

    public CityTempDTO(String city, String temperature, String wind, String description, List<ForecastDTO> forecast) {
        super(temperature, wind, description, forecast);
        this.city = city;
    }
}
