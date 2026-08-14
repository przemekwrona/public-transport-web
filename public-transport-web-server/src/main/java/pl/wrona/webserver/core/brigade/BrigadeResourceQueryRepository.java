package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrigadeResourceQueryRepository extends JpaRepository<BrigadeResourceEntity, Long> {

    List<BrigadeResourceEntity> findAllByBrigadeGroupBrigadeGroupIdOrderBySequenceAsc(Long brigadeGroupId);
}
