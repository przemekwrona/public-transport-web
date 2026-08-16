package pl.wrona.webserver.core.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TripSequenceQueryRepository extends JpaRepository<TripSequenceEntity, TripSequenceId> {

    @Query("""
            SELECT s FROM TripSequenceEntity s
            WHERE s.id.agencyCode = :agencyCode
            AND s.id.routeSequence = :routeSequence""")
    TripSequenceEntity findByAgencyCodeAndRouteSequence(
            @Param("agencyCode") String agencyCode,
            @Param("routeSequence") Integer routeSequence);

}
