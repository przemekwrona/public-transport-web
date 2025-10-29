package pl.wrona.webserver.bussiness.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.TripEntity;

import java.util.List;

@Repository
public interface TripQueryRepository extends JpaRepository<TripEntity, Long> {

    @Query("SELECT t FROM TripEntity t WHERE t.route.agency.agencyCode = :agencyCode AND t.route.line = :line AND t.route.name = :name")
    List<TripEntity> findByAgencyCodeAndLineAndName(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name);

    @Query("""
            SELECT t FROM TripEntity t
            WHERE t.route.agency.agencyCode = :agencyCode
                AND EXISTS (SELECT 1 FROM BrigadeTripEntity bd WHERE bd.rootTrip.tripId = t.tripId)""")
    List<TripEntity> findByExistsBrigade(@Param("agencyCode") String agencyCode);

}
