package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.agency.entity.RouteEntity;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, String> {

    @Query("SELECT r FROM RouteEntity r WHERE r.agency = :agency AND r.name = :name AND r.line = :line")
    RouteEntity findByAgencyCodeAndRouteId(@Param("agency") Agency agency, @Param("name") String name, @Param("line") String line);

    List<RouteEntity> findAllByAgencyOrderByLineAscNameAsc(Agency agency);

}
