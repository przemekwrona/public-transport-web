package pl.wrona.webserver.bussiness.trip.reader.route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.TripEntity;

import java.util.List;

@Repository
interface TripReaderByRouteRepository extends JpaRepository<TripEntity, Long> {

    @Query("SELECT t FROM TripEntity t WHERE t.route.line = :line AND t.route.name = :name AND t.route.agency = :agency")
    List<TripEntity> findAllByAgencyAndLineAndName(@Param("agency") AgencyEntity loggedAgencyEntity, @Param("line") String line, @Param("name") String name);
}
