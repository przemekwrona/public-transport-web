package pl.wrona.webserver.bussiness.route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;
import java.util.Set;

@Repository
public interface RouteQueryRepository extends JpaRepository<RouteEntity, String> {

    @Query("SELECT r FROM RouteEntity r WHERE r.agency.agencyCode = :agencyCode ORDER BY r.line")
    List<RouteEntity> findByAgencyCode(@Param("agencyCode") String agencyCode);

    @Query("SELECT r FROM RouteEntity r WHERE r.agency.agencyCode = :agencyCode AND r.line = :line AND r.name = :name AND r.version = :version")
    RouteEntity findByAgencyCodeAndLineAndName(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name, @Param("version") int version);

    @Deprecated
    @Query("SELECT r FROM RouteEntity r WHERE r.agency = :agency AND r.name = :name AND r.line = :line")
    RouteEntity findByAgencyCodeAndRouteId(@Param("agency") AgencyEntity agencyEntity, @Param("name") String name, @Param("line") String line);

    @Query("""
            SELECT r FROM TripEntity t JOIN t.route r
            WHERE r.agency.agencyCode = :agencyCode
                AND EXISTS (SELECT 1 FROM BrigadeTripEntity bd WHERE bd.rootTrip.tripId = t.tripId)""")
    List<RouteEntity> findByExistsBrigade(@Param("agencyCode") String agencyCode);

    @Query("""
            SELECT r.version FROM RouteEntity r
            WHERE r.agency.agencyCode = :agencyCode AND r.line = :line AND r.name = :name
            ORDER BY r.version DESC FETCH FIRST 1 ROWS ONLY""")
    Integer findLastInsertedVersion(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name);

    @Query("""
            SELECT r FROM RouteEntity r
            WHERE r.agency.agencyCode = :agencyCode
            AND EXISTS (SELECT 1 FROM TripEntity t WHERE t.route = r AND t.mainVariant AND t.variantMode = :tripVariantMode)""")
    Set<RouteEntity> findRouteByExistsTripVariant(@Param("agencyCode") String agencyCode, @Param("tripVariantMode") TripVariantMode tripVariantMode);

}
