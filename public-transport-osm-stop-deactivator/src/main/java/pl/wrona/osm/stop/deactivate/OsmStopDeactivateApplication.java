package pl.wrona.osm.stop.deactivate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class OsmStopDeactivateApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OsmStopDeactivateApplication.class, args);
        context.close();
    }

}
