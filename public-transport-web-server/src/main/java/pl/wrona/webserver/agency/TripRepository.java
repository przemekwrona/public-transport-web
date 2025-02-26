package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Route;
import pl.wrona.webserver.agency.entity.TripEntity;
import pl.wrona.webserver.agency.entity.TripVariantMode;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {

    TripEntity findFirstByRouteOrderByVariant(Route route);

    List<TripEntity> findAllByRoute(Route route);

    @Query("SELECT t FROM TripEntity t WHERE t.route.line LIKE CONCAT(:lineOrName,'%') OR t.route.name = CONCAT(:lineOrName,'%')")
    List<TripEntity> findByLineOrNameContainingIgnoreCase(@Param("lineOrName") String lineOrName);

    @Override
    void delete(TripEntity entity);

    TripEntity findAllByRouteAndVariantAndMode(Route route, String variant, TripVariantMode mode);

    @Modifying
    @Query("DELETE FROM TripEntity t WHERE t.route.line = :line AND t.route.name = :name AND t.variant = :variant AND t.mode = :mode")
    void deleteByLineAndNameAndVariantAndMode(@Param("line") String line, @Param("name") String name, @Param("variant") String variant, @Param("mode") TripVariantMode mode);

    @Query("SELECT t FROM TripEntity t WHERE t.route.line = :line AND t.route.name = :name AND t.variant = :variant AND t.mode = :mode")
    TripEntity findByLineAndNameAndVariantAndMode(@Param("line") String line, @Param("name") String name, @Param("variant") String variant, @Param("mode") TripVariantMode mode);
}
