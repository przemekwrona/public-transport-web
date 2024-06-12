package pl.wrona.webserver;

import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients
public class PublicTransportWebServerApplication {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.HEADERS;
    }

    public static void main(String[] args) {
        SpringApplication.run(PublicTransportWebServerApplication.class, args);
    }

}
