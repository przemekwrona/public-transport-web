package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.StopTimeEntity;
import pl.wrona.webserver.agency.entity.StopTimeId;
import pl.wrona.webserver.agency.entity.TripEntity;

import java.util.List;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTimeEntity, StopTimeId> {

    @Query("SELECT st FROM StopTimeEntity st WHERE st.stopTimeId.tripId = :tripId ORDER BY st.stopTimeId.stopSequence")
    List<StopTimeEntity> findAllByTripId(@Param("tripId") Long tripId);

    List<StopTimeEntity> findAllByTrip(TripEntity trip);

    @Modifying
    @Query("DELETE FROM StopTimeEntity st WHERE st.stopTimeId.tripId = :tripId")
    void deleteByTripId(@Param("tripId") Long tripId);
}
