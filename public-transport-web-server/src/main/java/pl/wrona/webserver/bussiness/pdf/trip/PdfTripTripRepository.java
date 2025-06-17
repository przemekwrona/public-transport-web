package pl.wrona.webserver.bussiness.pdf.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.TripEntity;

import java.util.List;

@Repository
interface PdfTripTripRepository extends JpaRepository<TripEntity, Long> {

    @Query("SELECT t FROM TripEntity t WHERE t.route.line = :line AND t.route.name = :name GROUP BY t.tripId")
    List<TripEntity> findAllByRoute(@Param("line") String line, @Param("name") String name);
}
