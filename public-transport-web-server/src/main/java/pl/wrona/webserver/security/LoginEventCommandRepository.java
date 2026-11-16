package pl.wrona.webserver.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginEventCommandRepository extends JpaRepository<LoginEventEntity, LoginEventId> {
}
