package pl.wrona.webserver.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarItemQueryRepository extends JpaRepository<CalendarItemEntity, Long> {

    @Query(value = "SELECT i FROM CalendarItemEntity i WHERE i.agency.agencyCode = :instance ORDER BY i.startDate DESC")
    List<CalendarItemEntity> findByAgency(@Param("instance") String instance);

    @Query("SELECT i FROM CalendarItemEntity  i WHERE i.agency.agencyCode = :instance AND i.sequence = :calendarSequence")
    CalendarItemEntity findByAgencyAndSequenceHexEquals(@Param("instance") String instance, @Param("calendarSequence") int calendarSequence);

}
