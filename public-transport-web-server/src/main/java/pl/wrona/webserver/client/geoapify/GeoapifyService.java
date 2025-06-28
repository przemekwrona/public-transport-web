package pl.wrona.webserver.client.geoapify;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.client.geoapify.geocode.Feature;
import pl.wrona.webserver.client.geoapify.geocode.GeoResponse;
import pl.wrona.webserver.client.geoapify.routing.RoutingResponse;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class GeoapifyService {

    private final GeoapifyClient geoapifyClient;

    @Value("${geoapify.client.key}")
    private String geoapifyApiKey;

    public GeoResponse geoapify(String address) {
        return this.geoapifyClient.geocode(address, geoapifyApiKey).getBody();
    }

    public Feature mostProbableAddress(String address) {
        return this.geoapify(address).features().stream()
                .max(Comparator.comparing(feature -> feature.properties().rank().confidence()))
                .orElse(null);
    }

    public Feature mostProbableAddress(String street, String houseNumber, String flatNumber, String postalCode, String postalCity) {
        String address = flatNumber == null || flatNumber.trim().isEmpty()
                ? "%s %s; %s %s; Polska".formatted(street, houseNumber, postalCode, postalCity)
                : "%s %s/%s; %s %s; Polska".formatted(street, houseNumber, flatNumber, postalCode, postalCity);

        return mostProbableAddress(address);
    }

    public RoutingResponse route(String waypoints) {
        return this.geoapifyClient.routing(waypoints, "drive", geoapifyApiKey).getBody();
    }
}
