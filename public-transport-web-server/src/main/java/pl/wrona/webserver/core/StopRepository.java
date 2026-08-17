package pl.wrona.webserver.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.entity.StopEntity;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<StopEntity, String> {

    List<StopEntity> findAllByStopIdIn(List<Long> stopIds);

    @Query("SELECT s FROM AgencyEntity a JOIN a.routeEntities r  JOIN r.tripEntities t JOIN t.stopTimes st JOIN st.stopEntity s WHERE :agency = a")
    List<StopEntity> findAllByAgency(@Param("agency") AgencyEntity agencyEntity);

    @Query("SELECT s FROM StopEntity s JOIN s.stopTimeEntities st WHERE st.tripProfile.trip.tripId IN :tripId")
    List<StopEntity> findAllByTripId(@Param("tripId") Long tripId);
}
