package pl.wrona.webserver.agency.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.model.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
}
