package com.rurbisservices.springreactive.weather;

import com.rurbisservices.springreactive.weather.service.impl.WeatherServiceImpl;
import com.rurbisservices.springreactive.weather.service.model.TemperatureDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.anyString;

@WebFluxTest
class WeatherApplicationTests {
	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private WeatherServiceImpl weatherService;

	@Test
	public void getTemperaturesTest() {
		Flux<TemperatureDTO> tempFlux = Flux.just(new TemperatureDTO("city1", "1", "1"),
				new TemperatureDTO("city2", "2", "2"));
		Mockito.when(weatherService.getTemperatureV2(anyString())).thenReturn(tempFlux);
		webTestClient.get().uri("/api/weather?city=city1,city2")
				.exchange()
				.expectStatus().isOk();
		Mockito.verify(weatherService, Mockito.times(1)).getTemperatureV2(anyString());
	}

}
