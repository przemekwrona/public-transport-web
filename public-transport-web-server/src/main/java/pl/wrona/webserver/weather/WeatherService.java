package pl.wrona.webserver.weather;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class WeatherService {

    private final WeatherClient weatherClient;

    @Cacheable(value = "weather", key = "{#city, #hour}")
    public WeatherResponse getWeather(String city, int hour) {
        return weatherClient.getCurrentWeather("05a9874c813049f5bb4174704241210", city);
    }
}
