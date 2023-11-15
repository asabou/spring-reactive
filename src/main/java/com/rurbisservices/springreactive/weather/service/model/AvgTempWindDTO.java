package com.rurbisservices.springreactive.weather.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.rurbisservices.springreactive.weather.utils.Constants.EMPTY_STRING;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvgTempWindDTO {
    private String temperature = EMPTY_STRING;
    private String wind = EMPTY_STRING;
}
