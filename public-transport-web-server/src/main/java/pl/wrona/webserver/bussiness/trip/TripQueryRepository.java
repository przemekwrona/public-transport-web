package pl.wrona.webserver.bussiness.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.TripEntity;
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
            AND t.variantName = :variantName
            AND t.variantMode = :variantMode""")
    boolean existsTripUniqueIndex(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name, @Param("variantName") String variantName, @Param("variantMode") TripVariantMode variantMode);

    @Query("""
            SELECT t FROM TripEntity t
            WHERE t.route.agency.agencyCode = :agencyCode
            AND t.route.line = :line
            AND t.route.name = :name
            AND t.variantName = :variantName
            AND t.variantMode = :variantMode""")
    TripEntity findTripByUniqueIndex(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name, @Param("variantName") String variantName, @Param("variantMode") TripVariantMode variantMode);

    @Query("""
            SELECT t FROM TripEntity t
            WHERE t.route.agency.agencyCode = :agencyCode
            AND t.route.routeCode = :routeCode
            AND t.tripCode = :tripCode""")
    TripEntity findTripByAgencyAndRouteCodeAndTripCode(@Param("agencyCode") String agencyCode, @Param("routeCode") String routeCode, @Param("tripCode") String tripCode);


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

    @Query("""
            SELECT t FROM TripEntity t
            WHERE t.route.routeId = :routeId
            ORDER BY t.createdAt ASC NULLS LAST, t.tripId ASC""")
    List<TripEntity> findByRouteIdOrderByCreatedAtAsc(@Param("routeId") Long routeId);
}