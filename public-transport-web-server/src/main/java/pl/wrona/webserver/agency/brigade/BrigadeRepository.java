package pl.wrona.webserver.agency.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrigadeRepository extends JpaRepository<BrigadeEntity, Long> {

    Optional<BrigadeEntity> findBrigadeEntitiesByBrigadeNumber(String brigadeNumber);
}
