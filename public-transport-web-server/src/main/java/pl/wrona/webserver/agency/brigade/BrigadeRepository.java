package pl.wrona.webserver.agency.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Agency;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrigadeRepository extends JpaRepository<BrigadeEntity, Long> {

    Optional<BrigadeEntity> findBrigadeEntitiesByAgencyAndBrigadeNumber(Agency agency, String brigadeNumber);

    List<BrigadeEntity> findAllByAgency(Agency agency);

    boolean existsBrigadeEntitiesByAgencyAndBrigadeNumber(Agency agency, String brigadeNumber);
}
