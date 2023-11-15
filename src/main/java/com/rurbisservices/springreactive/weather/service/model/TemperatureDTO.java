package com.rurbisservices.springreactive.weather.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemperatureDTO implements Comparable<TemperatureDTO> {
    private String name;
    private String temperature;
    private String wind;

    @Override
    public int compareTo(TemperatureDTO o) {
        return this.name.compareTo(o.name);
    }
}
