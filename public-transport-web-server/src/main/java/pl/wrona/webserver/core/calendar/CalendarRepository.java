package pl.wrona.webserver.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {

    List<CalendarEntity> findAllByAgency(AgencyEntity agencyEntity);

    @Query(value = "SELECT c FROM CalendarEntity c WHERE c.agency.agencyCode= :agency AND c.calendarName = :calendarName")
    Optional<CalendarEntity> findByAgencyAndCalendarName(@Param("agency") String agency, @Param("calendarName") String calendarName);

    boolean existsByAgencyAndCalendarName(AgencyEntity agencyEntity, String calendarName);

    List<CalendarEntity> findAllByAgencyAndCalendarNameStartingWith(AgencyEntity agencyEntity, String calendarName);

    List<CalendarEntity> findAllByAgencyAndStartDateBeforeAndEndDateAfter(AgencyEntity agencyEntity, LocalDate startDate, LocalDate endDate);
}
