package pl.wrona.webserver.bussiness.stoptime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripProfileEntity;

@Repository
public interface StopTimeCommandRepository extends JpaRepository<StopTimeEntity, StopTimeId> {

    @Modifying
    @Query("DELETE FROM StopTimeEntity st WHERE st.tripProfile.trip.tripId = :tripId")
    void deleteByTripId(@Param("tripId") Long tripId);

    void deleteAllByTripProfile(TripProfileEntity tripProfile);
}
