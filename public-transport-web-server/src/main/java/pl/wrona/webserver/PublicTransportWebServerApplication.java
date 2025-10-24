package pl.wrona.webserver;

import com.github.benmanes.caffeine.cache.Caffeine;
import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableJpaRepositories
@EnableScheduling
public class PublicTransportWebServerApplication {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.HEADERS;
    }

    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES);
    }

    @Bean
    public CacheManager cacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

        caffeineCacheManager.registerCustomCache("station_information", Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build());

        caffeineCacheManager.registerCustomCache("station_status", Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build());

        caffeineCacheManager.registerCustomCache("weather", Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build());

        return caffeineCacheManager;
    }

    public static void main(String[] args) {
        SpringApplication.run(PublicTransportWebServerApplication.class, args);
    }

}
