package pl.wrona.webserver.bussiness.gtfs.download;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GtfsCalendarRepository extends JpaRepository<CalendarSymbolEntity, Long> {

    @Query(value = "SELECT c FROM CalendarSymbolEntity c WHERE c.agency.agencyCode = :agency AND c.calendarItem.startDate <= :startDateIncluded")
    List<CalendarSymbolEntity> findAllByAgencyAndStartDateGreaterThanEqual(
            @Param("agency") String agency,
            @Param("startDateIncluded") LocalDate startDateIncluded);
}
