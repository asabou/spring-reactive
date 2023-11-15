package com.rurbisservices.springreactive.weather.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateBean {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
