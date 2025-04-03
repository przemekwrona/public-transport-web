package pl.wrona.webserver.agency.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Agency;

import java.util.List;

@Repository
public interface CalendarDatesRepository extends JpaRepository<CalendarDatesEntity, CalendarDatesId> {

    @Query("SELECT cd FROM CalendarDatesEntity cd WHERE cd.calendar.agency = :agency")
    List<CalendarDatesEntity> findAllByAgency(@Param("agency") Agency agency);

}
