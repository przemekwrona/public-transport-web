package pl.wrona.webserver.bussiness.gtfs.download;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.calendar.CalendarEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GtfsCalendarRepository extends JpaRepository<CalendarEntity, Long> {

    @Query(value = "SELECT c FROM CalendarEntity c WHERE c.agency.agencyCode = :agency AND c.startDate <= :startDateIncluded")
    List<CalendarEntity> findAllByAgencyAndStartDateGreaterThanEqual(
            @Param("agency") String agency,
            @Param("startDateIncluded") LocalDate startDateIncluded);
}
