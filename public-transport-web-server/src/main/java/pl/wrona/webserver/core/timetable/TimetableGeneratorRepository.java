package pl.wrona.webserver.core.timetable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableGeneratorRepository extends JpaRepository<TimetableGeneratorEntity, Long> {

    @Query("""
            SELECT t FROM TimetableGeneratorEntity t WHERE t.agency.agencyCode = :agencyCode""")
    List<TimetableGeneratorEntity> findAllByAgencyName(@Param("agencyCode") String agencyCode);
}
