package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Route;
import pl.wrona.webserver.agency.entity.TripEntity;
import pl.wrona.webserver.agency.entity.TripVariantMode;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {

    TripEntity findFirstByRouteOrderByVariant(Route route);

    List<TripEntity> findAllByRoute(Route route);

    TripEntity findAllByRouteAndVariantAndMode(Route route, String variant, TripVariantMode mode);
}
