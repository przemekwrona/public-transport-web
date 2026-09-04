package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface BrigadeEventCommandRepository extends JpaRepository<BrigadeEventEntity, Long> {

    void deleteAllByResourceIn(Collection<BrigadeResourceEntity> resources);
}
