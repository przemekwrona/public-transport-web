package pl.wrona.webserver.bussiness.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {

    @Query("SELECT t FROM TripEntity t JOIN t.tripProfiles WHERE t.route.agency = :agency AND t.route.routeCode = :routeCode")
    List<TripEntity> findByAgencyAndRouteCode(@Param("agency") AgencyEntity agencyEntity, @Param("routeCode") String routeCode);

    @Query("SELECT t FROM TripEntity t WHERE t.route.line = :line AND t.route.name = :name AND t.variantName = :variantName AND t.variantMode = :variantMode")
    TripEntity findByLineAndNameAndVariantAndMode(@Param("line") String line, @Param("name") String name, @Param("variantName") String variantName, @Param("variantMode") TripVariantMode variantMode);
}
