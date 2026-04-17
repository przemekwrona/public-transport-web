package pl.wrona.webserver.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarSymbolRepository extends JpaRepository<CalendarSymbolEntity, Long> {

    List<CalendarSymbolEntity> findAllByAgency(AgencyEntity agencyEntity);

    @Query(value = "SELECT s FROM CalendarSymbolEntity s WHERE s.calendarItem.agency.agencyCode= :agency AND s.calendarItem.calendarName = :calendarName")
    Optional<CalendarSymbolEntity> findByAgencyAndCalendarName(@Param("agency") String agency, @Param("calendarName") String calendarName);

    @Query(value = "SELECT s FROM CalendarSymbolEntity s WHERE s.calendarItem.agency= :agency AND s.calendarItem.calendarName LIKE CONCAT(:calendarName, '%')")
    List<CalendarSymbolEntity> findAllByAgencyAndCalendarNameStartingWith(@Param("agency") AgencyEntity agencyEntity, @Param("agency") String calendarName);

    CalendarSymbolEntity findByCalendarItemAndDesignationEquals(CalendarItemEntity calendarItemEntity, String designation);
}
