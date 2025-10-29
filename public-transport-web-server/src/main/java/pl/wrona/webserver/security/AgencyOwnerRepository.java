package pl.wrona.webserver.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;

@Repository
public interface AgencyOwnerRepository extends JpaRepository<AgencyEntity, Long> {

    AgencyEntity findAllByAppUser(AppUser appUserId);
}
