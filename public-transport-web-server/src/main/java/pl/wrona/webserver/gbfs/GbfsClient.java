package pl.wrona.webserver.gbfs;


import jakarta.annotation.Resource;
import org.igeolab.iot.gbfs.server.api.ApiV2Api;
import org.igeolab.iot.gbfs.server.api.model.StationInformationV23;
import org.igeolab.iot.gbfs.server.api.model.StationStatusV23;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Resource
@FeignClient(value = "${gbfs.client.warsaw.name}", url = "${gbfs.client.warsaw.url}")
public interface GbfsClient extends ApiV2Api {

    @GetMapping("/v2/{region}/{language}/station_information.json")
    ResponseEntity<StationInformationV23> v2RegionalStationInformationPublic(@PathVariable String region, @PathVariable String language);

    @GetMapping("/v2/{region}/{language}/station_status.json")
    ResponseEntity<StationStatusV23> v2RegionalStationStatusPublic(@PathVariable String region, @PathVariable String language);
}
