package pl.wrona.webserver.bussiness.gtfs.download;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.brigade.BrigadeTripEntity;

import java.util.List;

@Repository
public interface GtfsStopTimeRepository extends JpaRepository<StopTimeEntity, StopTimeId> {

    @Query(value = """
            SELECT st FROM StopTimeEntity st
            JOIN st.trip t
            JOIN t.brigadeTrips bt WHERE bt = :brigadeTrip""")
    List<StopTimeEntity> findAllByBrigadeTrip(@Param("brigadeTrip") BrigadeTripEntity brigadeTrip);
}
