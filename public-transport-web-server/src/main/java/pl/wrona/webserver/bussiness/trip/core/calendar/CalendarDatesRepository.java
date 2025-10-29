package pl.wrona.webserver.bussiness.trip.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;

import java.util.List;

@Repository
public interface CalendarDatesRepository extends JpaRepository<CalendarDatesEntity, CalendarDatesId> {

    @Query("SELECT cd FROM CalendarDatesEntity cd WHERE cd.calendar.agency = :agency")
    List<CalendarDatesEntity> findAllByAgency(@Param("agency") AgencyEntity agencyEntity);

    @Modifying
    @Query("DELETE FROM CalendarDatesEntity cd WHERE cd.calendar.agency = :agency AND cd.calendar = :calendarEntity")
    void deleteByAgencyAndCalendar(@Param("agency") AgencyEntity agencyEntity, @Param("calendarEntity") CalendarEntity calendarEntity);

}
