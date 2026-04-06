package pl.wrona.webserver.bussiness.location;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.LocationSearchResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.entity.TerritorialUnitRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class LocationQueryService {

    private TerritorialUnitRepository territorialUnitRepository;

    public LocationSearchResponse findLocationByName(String q) {
        var locations = territorialUnitRepository.findAllByNazwaLike(q);
        return new LocationSearchResponse()
                .locations(List.of());
    }
}
