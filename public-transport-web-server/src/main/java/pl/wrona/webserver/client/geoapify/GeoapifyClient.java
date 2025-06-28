package pl.wrona.webserver.client.geoapify;

import jakarta.annotation.Resource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.wrona.webserver.client.geoapify.geocode.GeoResponse;
import pl.wrona.webserver.client.geoapify.routing.RoutingResponse;

@Resource
@FeignClient(name = "${geoapify.client.name}", url = "${geoapify.client.url}")
public interface GeoapifyClient {

    @GetMapping("/v1/geocode/search")
    ResponseEntity<GeoResponse> geocode(@RequestParam("text") String text, @RequestParam("apiKey") String apiKey);

    @GetMapping("/v1/routing")
    ResponseEntity<RoutingResponse> routing(@RequestParam("waypoints") String waypoints, @RequestParam("mode") String mode, @RequestParam("max_speed") int maxSped, @RequestParam("apiKey") String apiKey);
}
