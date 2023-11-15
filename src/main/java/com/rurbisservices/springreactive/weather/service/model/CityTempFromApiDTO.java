package com.rurbisservices.springreactive.weather.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.rurbisservices.springreactive.weather.utils.Constants.EMPTY_STRING;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityTempFromApiDTO {
    private String temperature = EMPTY_STRING;
    private String wind = EMPTY_STRING;
    private String description = EMPTY_STRING;
    private List<ForecastDTO> forecast = new ArrayList<>();
}
