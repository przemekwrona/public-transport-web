package pl.wrona.webserver.agency;

import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripMode;
import pl.wrona.webserver.agency.entity.TripVariantMode;

@UtilityClass
public class TripModeMapper {

    TripMode map(TripVariantMode mode) {
        if (TripVariantMode.MAIN.equals(mode)) {
            return TripMode.MAIN;
        } else if (TripVariantMode.BACK.equals(mode)) {
            return TripMode.BACK;
        } else {
            return null;
        }
    }

    TripVariantMode map(TripMode mode) {
        if (TripMode.MAIN.equals(mode)) {
            return TripVariantMode.MAIN;
        } else if (TripMode.BACK.equals(mode)) {
            return TripVariantMode.BACK;
        } else {
            return null;
        }
    }
}
