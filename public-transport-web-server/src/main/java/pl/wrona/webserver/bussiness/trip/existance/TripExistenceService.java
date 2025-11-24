package pl.wrona.webserver.bussiness.trip.existance;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.trip.TripQueryRepository;
import pl.wrona.webserver.core.mapper.TripModeMapper;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class TripExistenceService {

    private final TripQueryRepository tripQueryRepository;


    @PreAgencyAuthorize
    public Status hasVariantDetails(String agency, TripId tripId) {
        boolean variantAlreadyExists = tripQueryRepository.existsTripUniqueIndex(agency, tripId.getRouteId().getLine(), tripId.getRouteId().getName(), tripId.getVariantName(), TripModeMapper.map(tripId.getVariantMode()), TripTrafficModeMapper.map(tripId.getTrafficMode()));
        return variantAlreadyExists ? new Status().status(Status.StatusEnum.EXISTS) : new Status().status(Status.StatusEnum.NOT_EXIST);
    }
}
