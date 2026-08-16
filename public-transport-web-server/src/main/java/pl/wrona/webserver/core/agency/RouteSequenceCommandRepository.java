package pl.wrona.webserver.core.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteSequenceCommandRepository extends JpaRepository<RouteSequenceEntity, String> {
}
