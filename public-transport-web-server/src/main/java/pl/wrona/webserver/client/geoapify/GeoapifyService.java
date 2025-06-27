package pl.wrona.webserver.client.geoapify;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeoapifyService {

    private final GeoapifyClient geoapifyClient;

    @Value("${geoapify.client.key}")
    private String geoapifyApiKey;

    public GeoResponse geoapify(String address) {
        return this.geoapifyClient.geocode(address, geoapifyApiKey).getBody();
    }
}
