package pl.wrona.webserver.core.timetable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimetableGeneratorQueryRepository extends JpaRepository<TimetableGeneratorEntity, Long> {

    @Query("""
            SELECT t FROM TimetableGeneratorEntity t WHERE t.agency.agencyCode = :agencyCode""")
    List<TimetableGeneratorEntity> findAllByAgencyName(@Param("agencyCode") String agencyCode);

    @Query("""
            SELECT t FROM TimetableGeneratorEntity t
            WHERE t.agency.agencyCode = :agencyCode
            AND t.route.line = :line
            AND t.route.name = :name
            AND t.route.version = :version
            AND t.createdAt = :createdAt""")
    TimetableGeneratorEntity findByAgencyAndRouteIdAndCreateDate(@Param("agencyCode") String agencyCode, @Param("line") String line, @Param("name") String name, @Param("version") int version, @Param("createdAt") LocalDateTime createdAt);
}