package pl.wrona.webserver.core.mapper;

import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.TripMode;
import pl.wrona.webserver.core.entity.TripVariantMode;

@UtilityClass
public class TripModeMapper {

    public TripMode map(TripVariantMode mode) {
        if (TripVariantMode.FRONT.equals(mode)) {
            return TripMode.FRONT;
        } else if (TripVariantMode.BACK.equals(mode)) {
            return TripMode.BACK;
        } else {
            return null;
        }
    }

    public TripVariantMode map(TripMode mode) {
        if (TripMode.FRONT.equals(mode)) {
            return TripVariantMode.FRONT;
        } else if (TripMode.BACK.equals(mode)) {
            return TripVariantMode.BACK;
        } else {
            return null;
        }
    }
}
