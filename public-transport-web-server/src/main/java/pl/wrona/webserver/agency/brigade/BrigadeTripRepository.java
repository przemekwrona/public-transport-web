package pl.wrona.webserver.agency.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrigadeTripRepository extends JpaRepository<BrigadeTripEntity, Long> {
}
