package pl.wrona.webserver.bussiness.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;

import java.util.List;

@Repository
public interface TripProfileQueryRepository extends JpaRepository<TripProfileEntity, Long> {

    List<TripProfileEntity> findAllByTrip(TripEntity trip);

    @Query("SELECT tpe FROM TripProfileEntity tpe WHERE tpe.trip.route = :routeEntity")
    List<TripProfileEntity> findAllByRoute(RouteEntity routeEntity);

    TripProfileEntity findAllByTripAndTrafficMode(TripEntity trip, TripTrafficMode trafficMode);

    @Query("SELECT p FROM TripProfileEntity p WHERE p.trip.route.agency.agencyCode = :agencyCode AND p.trip.route.routeSequence = :routeSequence AND p.trip.tripSequence = :tripSequence AND p.trafficMode = :trafficMode")
    TripProfileEntity findByAgencyAndRouteCodeAndTripCodeAndTrafficMode(@Param("agencyCode") String agency, @Param("routeSequence") int routeCode, @Param("tripSequence") int tripSequence, @Param("trafficMode") TripTrafficMode trafficMode);

}
