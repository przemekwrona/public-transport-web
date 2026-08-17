package pl.wrona.webserver.bussiness.pdf.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;

import java.util.List;

@Repository
interface PdfTripStopTimeRepository extends JpaRepository<StopTimeEntity, StopTimeId> {

    @Query("SELECT st FROM StopTimeEntity st JOIN st.tripProfile.trip t WHERE t = :trip")
    List<StopTimeEntity> findAllByTrip(TripEntity trip);
}
