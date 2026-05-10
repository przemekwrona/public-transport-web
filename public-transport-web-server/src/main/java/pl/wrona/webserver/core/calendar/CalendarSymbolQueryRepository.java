package pl.wrona.webserver.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarSymbolQueryRepository extends JpaRepository<CalendarSymbolEntity, Long> {

    List<CalendarSymbolEntity> findAllByAgency(AgencyEntity agencyEntity);

    @Query(value = "SELECT s FROM CalendarSymbolEntity s WHERE s.calendarItem.agency.agencyCode= :agency AND s.calendarItem.calendarName = :calendarName AND s.designation = :designation")
    Optional<CalendarSymbolEntity> findByAgencyAndCalendarNameAndDesignation(@Param("agency") String agency, @Param("calendarName") String calendarName, @Param("designation") String designation);

    @Query(value = "SELECT s FROM CalendarSymbolEntity s WHERE s.calendarItem.agency= :agency AND s.calendarItem.calendarName LIKE CONCAT(:calendarName, '%')")
    List<CalendarSymbolEntity> findAllByAgencyAndCalendarNameStartingWith(@Param("agency") AgencyEntity agencyEntity, @Param("agency") String calendarName);

    CalendarSymbolEntity findByCalendarItemAndDesignationEquals(CalendarItemEntity calendarItemEntity, String designation);

    @Query(value = "SELECT s FROM CalendarSymbolEntity s WHERE s.calendarItem.agency.agencyCode = :agency AND s.calendarItem.calendarName IN :calendarNames")
    List<CalendarSymbolEntity> findAllByAgencyAndCalendarNamesIn(@Param("agency") String agency, @Param("calendarNames") List<String> calendarNames);
}
