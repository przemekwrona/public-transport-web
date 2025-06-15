package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public interface GoogleAgreementsRepository extends JpaRepository<GoogleAgreementEntity, Long> {

    @Query("SELECT agreements FROM GoogleAgreementEntity agreements WHERE agreements.agency.agencyId = :agencyId")
    Optional<GoogleAgreementEntity> findByAgency(@Param("agencyId") Long agencyId);
}
