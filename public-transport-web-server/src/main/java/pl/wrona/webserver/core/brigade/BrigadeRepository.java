package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrigadeRepository extends JpaRepository<BrigadeEntity, Long> {

    Optional<BrigadeEntity> findBrigadeEntitiesByAgencyAndBrigadeNumber(AgencyEntity agencyEntity, String brigadeNumber);

    List<BrigadeEntity> findAllByAgency(AgencyEntity agencyEntity);

    boolean existsBrigadeEntitiesByAgencyAndBrigadeNumber(AgencyEntity agencyEntity, String brigadeNumber);
}
