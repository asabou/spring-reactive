package com.rurbisservices.springreactive.weather.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForecastDTO {
    private String day;
    private String temperature;
    private String wind;
}
