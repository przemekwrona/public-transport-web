package pl.wrona.webserver.bussiness.pdf.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.brigade.BrigadeTripEntity;
import pl.wrona.webserver.agency.entity.TripEntity;

import java.util.List;

@Repository
public interface PdfTripBrigadeTripRepository extends JpaRepository<BrigadeTripEntity, String> {

    List<BrigadeTripEntity> findByRootTrip(TripEntity rootTrip);
}
