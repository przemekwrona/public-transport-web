package pl.wrona.webserver.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarSequenceQueryRepository extends JpaRepository<CalendarSequenceEntity, String> {

    @Query("SELECT s FROM CalendarSequenceEntity s WHERE s.agencyCode = :agencyCode")
    CalendarSequenceEntity findByAgencyCode(@Param("agencyCode") String agencyCode);

}
