package pl.wrona.webserver.core.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteQueryRepository extends JpaRepository<RouteEntity, String> {

    @Query("SELECT r FROM RouteEntity r WHERE r.agency.agencyCode = :agencyCode")
    List<RouteEntity> findByAgencyCode(@Param("agencyCode") String agencyCode);

    @Query("SELECT r FROM RouteEntity r WHERE r.agency.agencyCode = :agencyCode AND r.line = :line AND r.name = :name")
    RouteEntity findByAgencyCodeAndLineAndName(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name);

    @Deprecated
    @Query("SELECT r FROM RouteEntity r WHERE r.agency = :agency AND r.name = :name AND r.line = :line")
    RouteEntity findByAgencyCodeAndRouteId(@Param("agency") AgencyEntity agencyEntity, @Param("name") String name, @Param("line") String line);

    @Deprecated
    List<RouteEntity> findAllByAgencyOrderByLineAscNameAsc(AgencyEntity agencyEntity);

}
