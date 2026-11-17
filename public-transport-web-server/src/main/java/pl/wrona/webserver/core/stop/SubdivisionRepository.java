package pl.wrona.webserver.core.stop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SubdivisionRepository extends JpaRepository<SubdivisionEntity, String> {

    List<SubdivisionEntity> findAllByTerytIn(Collection<String> teryts);
}
