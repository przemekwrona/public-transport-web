package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Stop;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, String> {

//    @Query("SELECT FROM Stop")
    List<Stop> findAllByStopIdIn(List<String> bdot10kId);
}
