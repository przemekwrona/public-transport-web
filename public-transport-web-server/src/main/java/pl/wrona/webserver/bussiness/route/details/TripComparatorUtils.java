package pl.wrona.webserver.bussiness.route.details;

import lombok.experimental.UtilityClass;
import pl.wrona.webserver.core.agency.TripEntity;

import java.util.Comparator;

@UtilityClass
public class TripComparatorUtils {

    public static Comparator<TripEntity> tripEntityComparator() {
        return Comparator.comparing(TripEntity::isMainVariant)
                .thenComparingInt((TripEntity t) -> t.getVariantMode().getWeight())
                .thenComparing((TripEntity t) -> t.getTrafficMode().getWeight()).reversed();
    }
}
