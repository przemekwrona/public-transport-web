package pl.wrona.osm.stop.deactivate.stop;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<StopEntity, Long> {

    List<StopEntity> findAllByDeactivatedCheckedIsFalseAndOsmIsTrue(Pageable pageable);

    boolean existsByDeactivatedCheckedIsFalseAndOsmIsTrue();

    @Query(value = "SELECT count(*) > 0 hasBdot10kStop FROM stop osm_stop WHERE osm_stop.stop_id = :stopId AND EXISTS (SELECT 1 FROM stop AS bdot10k_stop WHERE osm_stop.stop_id != bdot10k_stop.stop_id AND bdot10k_stop.is_bdot10k IS TRUE AND (6371000 * 2 * ASIN(SQRT(POWER(SIN(RADIANS(bdot10k_stop.lat - osm_stop.lat) / 2), 2) + COS(RADIANS(osm_stop.lat)) * COS(RADIANS(bdot10k_stop.lat)) * POWER(SIN(RADIANS(bdot10k_stop.lon - osm_stop.lon) / 2), 2)))) < 10)", nativeQuery = true)
    boolean existsByBdot10kStopIn10m(@Param("stopId") Long stopId);
}
