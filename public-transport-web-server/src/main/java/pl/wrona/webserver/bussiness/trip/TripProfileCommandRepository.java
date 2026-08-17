package pl.wrona.webserver.bussiness.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.TripProfileEntity;

@Repository
public interface TripProfileCommandRepository extends JpaRepository<TripProfileEntity, Long> {
}
