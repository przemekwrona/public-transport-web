package pl.wrona.webserver.agency.stop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.model.Stop;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, Long> {

    List<Stop> findAllByOsmIdIn(List<Long> osmIds);
}
