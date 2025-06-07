package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.agency.entity.StopEntity;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<StopEntity, String> {

    List<StopEntity> findAllByStopIdIn(List<Long> stopIds);

    @Query("SELECT s FROM Agency a JOIN a.routeEntities r  JOIN r.tripEntities t JOIN t.stopTimes st JOIN st.stopEntity s WHERE :agency = a")
    List<StopEntity> findAllByAgency(@Param("agency") Agency agency);
}
