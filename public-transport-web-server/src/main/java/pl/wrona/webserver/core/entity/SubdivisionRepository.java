package pl.wrona.webserver.core.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubdivisionRepository extends JpaRepository<SubdivisionEntity, String> {
}
