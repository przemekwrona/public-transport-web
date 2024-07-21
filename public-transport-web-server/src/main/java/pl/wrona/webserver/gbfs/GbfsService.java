package pl.wrona.webserver.gbfs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.igeolab.iot.gbfs.server.api.model.StationInformationV23;
import org.igeolab.iot.gbfs.server.api.model.StationStatusV23;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class GbfsService {

    private final GbfsClient gbfsClient;

    public StationInformationV23 v2RegionalStationInformationPublic(String regionName) {
        return this.gbfsClient.v2RegionalStationInformationPublic(regionName, "pl").getBody();
    }

    public StationStatusV23 v2RegionalStationStatusPublic(String regionName) {
        return this.gbfsClient.v2RegionalStationStatusPublic(regionName, "pl").getBody();
    }

}
