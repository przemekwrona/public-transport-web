package pl.wrona.webserver.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;

import java.util.Collection;
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

    @Query("""
            SELECT st FROM StopTimeEntity st
            JOIN FETCH st.stopEntity
            JOIN FETCH st.tripProfile tp
            WHERE tp.tripProfileId IN :tripProfileIds
            AND (
                st.stopTimeId.stopSequence = (
                    SELECT MIN(stMin.stopTimeId.stopSequence)
                    FROM StopTimeEntity stMin
                    WHERE stMin.tripProfile.tripProfileId = tp.tripProfileId
                )
                OR st.stopTimeId.stopSequence = (
                    SELECT MAX(stMax.stopTimeId.stopSequence)
                    FROM StopTimeEntity stMax
                    WHERE stMax.tripProfile.tripProfileId = tp.tripProfileId
                )
            )
            """)
    List<StopTimeEntity> findFirstAndLastByTripProfileIds(@Param("tripProfileIds") Collection<Long> tripProfileIds);
}
