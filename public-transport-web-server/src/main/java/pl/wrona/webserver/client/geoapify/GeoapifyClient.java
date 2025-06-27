package pl.wrona.webserver.client.geoapify;

import jakarta.annotation.Resource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Resource
@FeignClient(name = "${geoapify.client.name}", url = "${geoapify.client.url}")
public interface GeoapifyClient {

    @GetMapping("/v1/geocode/search")
    ResponseEntity<GeoResponse> geocode(@RequestParam("text") String text, @RequestParam("apiKey") String apiKey);
}
