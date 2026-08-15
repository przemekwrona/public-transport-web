package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrigadeItemSequenceCommandRepository extends JpaRepository<BrigadeItemSequenceEntity, BrigadeItemSequenceId> {
}
