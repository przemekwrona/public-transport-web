package pl.wrona.webserver.stops;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusStopRepository extends JpaRepository<BusStop, Long> {

    @Query(value = "SELECT s FROM BusStop s WHERE s.lat < :maxLat and s.lon > :minLon and s.lat > :minLat and s.lon < :maxLon")
    List<BusStop> findAllByArea(@Param("maxLat") float maxLat, @Param("minLon") float minLon, @Param("minLat") float minLat, @Param("maxLon") float maxLon);

    List<BusStop> findBusStopByNameStartsWith(String name);
}
