package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BrigadeEventQueryRepository extends JpaRepository<BrigadeEventEntity, Long> {

    List<BrigadeEventEntity> findAllByResourceBrigadeResourceIdInOrderByStartSecondAsc(Collection<Long> resourceIds);
}
