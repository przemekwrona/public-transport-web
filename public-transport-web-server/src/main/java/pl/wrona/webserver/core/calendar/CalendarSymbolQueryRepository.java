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
public interface CalendarSymbolQueryRepository extends JpaRepository<CalendarSymbolEntity, Long> {

    List<CalendarSymbolEntity> findAllByAgency(AgencyEntity agencyEntity);

    @Query(value = "SELECT s FROM CalendarSymbolEntity s WHERE s.calendarItem.agency.agencyCode= :agency AND s.calendarItem.calendarName = :calendarName AND s.designation = :designation")
    Optional<CalendarSymbolEntity> findByAgencyAndCalendarNameAndDesignation(@Param("agency") String agency, @Param("calendarName") String calendarName, @Param("designation") String designation);

    CalendarSymbolEntity findByCalendarItemAndDesignationEquals(CalendarItemEntity calendarItemEntity, String designation);

    @Query(value = "SELECT s FROM CalendarSymbolEntity s WHERE s.calendarItem.agency.agencyCode = :agency AND s.calendarItem.calendarName IN :calendarNames ORDER BY s.calendarItem.startDate ASC")
    List<CalendarSymbolEntity> findAllByAgencyAndCalendarNamesIn(@Param("agency") String agency, @Param("calendarNames") List<String> calendarNames);

    @Query(value = "SELECT s FROM CalendarSymbolEntity s WHERE s.calendarItem.agency.agencyCode = :agency AND s.calendarItem.startDate = :startDate AND s.calendarItem.endDate = :endDate")
    List<CalendarSymbolEntity> findAllByAgencyAndStartDateAndEndDate(@Param("agency") String agency, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT s FROM CalendarSymbolEntity s
            WHERE s.calendarItem.agency.agencyCode = :agency
            AND s.calendarItem.sequenceHex = :calendarCode
            AND s.designation = :calendarSymbol""")
    CalendarSymbolEntity findByAgencyAndBrigadeAndCalendarAndSymbol(@Param("agency") String agency, @Param("calendarCode") String calendarCode, @Param("calendarSymbol") String calendarSymbol);


    @Query("""
            SELECT g.calendarSymbol FROM BrigadeGroupEntity g
            JOIN g.calendarSymbol s
            WHERE g.brigadeItem.agency.agencyCode = :agency
            AND g.brigadeItem.brigadeItemCode = :brigadeCode
            AND s.calendarItem.sequenceHex = :calendarCode
            AND s.designation = :calendarSymbol""")
    CalendarSymbolEntity findByAgencyAndBrigadeAndCalendarAndSymbol(@Param("agency") String agency, @Param("brigadeCode") String brigadeCode, @Param("calendarCode") String calendarCode, @Param("calendarSymbol") String calendarSymbol);

    @Query("""
            SELECT s FROM CalendarSymbolEntity s
            WHERE s.calendarItem.agency.agencyCode = :agencyCode
            AND s.calendarItem.sequence = :calendarSequence""")
    List<CalendarSymbolEntity> findByAgencyAndCalendarCode(@Param("agencyCode") String agencyCode, @Param("calendarSequence") int calendarSequence);

}
