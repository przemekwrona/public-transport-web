package pl.wrona.webserver.bussiness.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;

import java.util.List;

@Repository
public interface TripProfileQueryRepository extends JpaRepository<TripProfileEntity, Long> {

    List<TripProfileEntity> findAllByTrip(TripEntity trip);

}
