package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.agency.entity.Route;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {

    @Query("SELECT r FROM Route r WHERE r.agency = :agency AND r.name = :name AND r.line = :line")
    Route findByAgencyCodeAndRouteId(@Param("agency") Agency agency, @Param("name") String name, @Param("line") String line);

    List<Route> findAllByAgencyOrderByLineAscNameAsc(Agency agency);

}
