package pl.wrona.webserver.bussiness.pdf.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.StopTimeEntity;
import pl.wrona.webserver.agency.entity.StopTimeId;
import pl.wrona.webserver.agency.entity.TripEntity;

import java.util.List;

@Repository
interface PdfTripStopTimeRepository extends JpaRepository<StopTimeEntity, StopTimeId> {

    List<StopTimeEntity> findAllByTrip(TripEntity trip);
}
