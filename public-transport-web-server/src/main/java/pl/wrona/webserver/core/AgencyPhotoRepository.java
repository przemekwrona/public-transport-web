package pl.wrona.webserver.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.AgencyPhotoEntity;

@Repository
public interface AgencyPhotoRepository extends JpaRepository<AgencyPhotoEntity, Long> {

    AgencyPhotoEntity findFirstByAgencyOrderByCreatedAtDesc(AgencyEntity agency);

    void deleteAllByAgency(AgencyEntity agency);
}
