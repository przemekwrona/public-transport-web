package pl.wrona.webserver.core.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StopAgencyRepository extends JpaRepository<StopAgencyEntity, Long> {

    Optional<StopAgencyEntity> findByAgency_AgencyIdAndStop_StopId(Long agencyId, Long stopId);

    @Query("""
            select agencyStop from StopAgencyEntity agencyStop
            join fetch agencyStop.stop
            where agencyStop.agency.agencyId = :agencyId
              and agencyStop.stop.stopId in :stopIds
            """)
    List<StopAgencyEntity> findByAgencyIdAndStopIds(@Param("agencyId") Long agencyId, @Param("stopIds") Collection<Long> stopIds);

    @Query("""
            select agencyStop from StopAgencyEntity agencyStop
            join fetch agencyStop.stop
            where agencyStop.agency.agencyCode = :agencyCode
              and lower(agencyStop.name) like lower(concat(:prefix, '%'))
            """)
    List<StopAgencyEntity> findByAgencyCodeAndNameStartingWith(@Param("agencyCode") String agencyCode, @Param("prefix") String prefix);
}
