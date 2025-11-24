package pl.wrona.webserver.bussiness.pdf.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;

@Repository
interface PdfTripTripRepository extends JpaRepository<TripEntity, Long> {

    @Query("SELECT t FROM TripEntity t WHERE t.route.line = :line AND t.route.name = :name AND t.variantMode = :variantMode  GROUP BY t.tripId")
    List<TripEntity> findAllByRoute(@Param("line") String line, @Param("name") String name, @Param("variantMode") TripVariantMode variantMode);

    @Query("SELECT t.route.line AS line, t.route.name AS name, t.variantMode AS variantMode FROM TripEntity t WHERE t.route.line = :line AND t.route.name = :name GROUP BY t.route.line, t.route.name, t.variantMode")
    List<TripMode> findAllByRoute(@Param("line") String line, @Param("name") String name);
}
