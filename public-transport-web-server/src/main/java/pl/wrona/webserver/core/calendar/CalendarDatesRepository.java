package pl.wrona.webserver.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.entity.Agency;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarDatesRepository extends JpaRepository<CalendarDatesEntity, CalendarDatesId> {

    @Query("SELECT cd FROM CalendarDatesEntity cd WHERE cd.calendar.agency = :agency")
    List<CalendarDatesEntity> findAllByAgency(@Param("agency") Agency agency);

    @Query("SELECT cd FROM CalendarDatesEntity cd WHERE cd.calendar.agency = :agency AND cd.calendar.startDate <= :startDate AND cd.calendar.endDate >= :endDate")
    List<CalendarDatesEntity> findActiveCalendarDate(@Param("agency") Agency agency, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Modifying
    @Query("DELETE FROM CalendarDatesEntity cd WHERE cd.calendar.agency = :agency AND cd.calendar = :calendarEntity")
    void deleteByAgencyAndCalendar(@Param("agency") Agency agency, @Param("calendarEntity") CalendarEntity calendarEntity);

}
