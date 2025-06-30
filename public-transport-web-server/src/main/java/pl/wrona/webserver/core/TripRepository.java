package pl.wrona.webserver.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {

    TripEntity findFirstByRouteOrderByVariantName(RouteEntity routeEntity);

    List<TripEntity> findAllByRoute(RouteEntity routeEntity);

    @Query("SELECT t FROM TripEntity t WHERE t.route.agency = :agency AND t.route.line LIKE CONCAT(:lineOrName,'%') OR t.route.name = CONCAT(:lineOrName,'%')")
    List<TripEntity> findByLineOrNameContainingIgnoreCase(@Param("lineOrName") String lineOrName, @Param("agency") AgencyEntity agencyEntity);

    @Override
    void delete(TripEntity entity);

    TripEntity findAllByRouteAndVariantNameAndMode(RouteEntity routeEntity, String variantName, TripVariantMode mode);

    @Modifying
    @Query("DELETE FROM TripEntity t WHERE t.route.line = :line AND t.route.name = :name AND t.variantName = :variantName AND t.mode = :mode")
    void deleteByLineAndNameAndVariantAndMode(@Param("line") String line, @Param("name") String name, @Param("variantName") String variantName, @Param("mode") TripVariantMode mode);

    @Query("SELECT t FROM TripEntity t WHERE t.route.line = :line AND t.route.name = :name AND t.variantName = :variantName AND t.mode = :mode")
    TripEntity findByLineAndNameAndVariantAndMode(@Param("line") String line, @Param("name") String name, @Param("variantName") String variantName, @Param("mode") TripVariantMode mode);
}
