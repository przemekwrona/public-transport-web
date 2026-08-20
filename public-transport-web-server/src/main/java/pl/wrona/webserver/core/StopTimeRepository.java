package pl.wrona.webserver.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;

import java.util.List;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTimeEntity, StopTimeId> {

    @Query("""
            SELECT st FROM StopTimeEntity st
            JOIN FETCH st.tripProfile tp
            JOIN FETCH st.stopEntity
            WHERE tp.trip.tripId = :tripId
            ORDER BY st.stopTimeId.stopSequence
            """)
    List<StopTimeEntity> findAllByTripId(@Param("tripId") Long tripId);

    @Query("SELECT st FROM StopTimeEntity st JOIN st.tripProfile.trip t WHERE t = :trip")
    List<StopTimeEntity> findAllByTrip(TripEntity trip);
}
