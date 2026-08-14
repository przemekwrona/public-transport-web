package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrigadeResourceCommandRepository extends JpaRepository<BrigadeResourceEntity, Long> {
}
