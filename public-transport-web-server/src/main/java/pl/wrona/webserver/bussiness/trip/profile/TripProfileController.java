package pl.wrona.webserver.bussiness.trip.profile;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.TripProfileApi;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateTripProfileRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class TripProfileController implements TripProfileApi {

    private final TripProfileUpdaterService tripProfileUpdaterService;

    @Override
    public ResponseEntity<Status> updateTripProfile(String agency, String routeCode, String tripCode, UpdateTripProfileRequest updateTripProfileRequest) {
        return ResponseEntity.ok(tripProfileUpdaterService.updateTripProfile(agency, routeCode, tripCode, updateTripProfileRequest));
    }

    @Override
    public ResponseEntity<Status> updateTripProfiles(String agency, String routeCode, String tripCode, UpdateTripProfileRequest updateTripProfileRequest) {
        return ResponseEntity.ok(tripProfileUpdaterService.updateTripProfiles(agency, routeCode, tripCode, updateTripProfileRequest));
    }
}
