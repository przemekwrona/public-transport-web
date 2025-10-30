package pl.wrona.webserver.bussiness.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;

@Repository
public interface TripQueryRepository extends JpaRepository<TripEntity, Long> {

    @Query("SELECT t FROM TripEntity t WHERE t.route.agency.agencyCode = :agencyCode AND t.route.line = :line AND t.route.name = :name")
    List<TripEntity> findByAgencyCodeAndLineAndName(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name);

    @Query("""
            SELECT CASE WHEN (COUNT(*) > 0) THEN TRUE ELSE FALSE END
            FROM TripEntity t
            WHERE t.route.agency.agencyCode = :agencyCode
            AND t.route.line = :line
            AND t.route.name = :name
            AND t.mode = :mode
            AND t.trafficMode = :trafficMode""")
    boolean existsTripUniqueIndex(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name, @Param("mode") TripVariantMode mode, @Param("trafficMode") TripTrafficMode trafficMode);

    @Query("""
            SELECT t FROM TripEntity t
            WHERE t.route.agency.agencyCode = :agencyCode
            AND t.route.line = :line
            AND t.route.name = :name
            AND t.mode = :mode
            AND t.trafficMode = :trafficMode""")
    TripEntity findTripByUniqueIndex(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name, @Param("mode") TripVariantMode mode, @Param("trafficMode") TripTrafficMode trafficMode);


    @Query("""
            SELECT CASE WHEN (COUNT(*) > 0) THEN TRUE ELSE FALSE END
            FROM BrigadeTripEntity b
            WHERE b.rootTrip = :trip""")
    boolean existsTripInBrigade(@Param("trip") TripEntity trip);

    @Query("""
            SELECT t FROM TripEntity t
            WHERE t.route.agency.agencyCode = :agencyCode
                AND EXISTS (SELECT 1 FROM BrigadeTripEntity bd WHERE bd.rootTrip.tripId = t.tripId)""")
    List<TripEntity> findByExistsBrigade(@Param("agencyCode") String agencyCode);
}