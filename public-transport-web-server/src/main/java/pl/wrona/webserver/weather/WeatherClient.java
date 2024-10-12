package pl.wrona.webserver.weather;

import jakarta.annotation.Resource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Resource
@FeignClient(value = "weather-client", url = "${weather.client.url}")
public interface WeatherClient {


    @GetMapping("/v1/current.json")
    WeatherResponse getCurrentWeather(@RequestParam(value = "key") String key, @RequestParam(value = "q") String q);
}
