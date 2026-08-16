package pl.wrona.webserver.core.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteSequenceQueryRepository extends JpaRepository<RouteSequenceEntity, String> {

    @Query("SELECT s FROM RouteSequenceEntity s WHERE s.agencyCode = :agencyCode")
    RouteSequenceEntity findByAgencyCode(@Param("agencyCode") String agencyCode);

}
