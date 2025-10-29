package pl.wrona.webserver.bussiness.pdf.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;

import java.util.List;

@Repository
interface PdfTripStopTimeRepository extends JpaRepository<StopTimeEntity, StopTimeId> {

    List<StopTimeEntity> findAllByTrip(TripEntity trip);
}
