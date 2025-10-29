package pl.wrona.webserver.bussiness.gtfs.download;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.brigade.BrigadeTripEntity;
import pl.wrona.webserver.core.calendar.CalendarEntity;

import java.util.List;

@Repository
public interface GtfsBrigadeTripRepository extends JpaRepository<BrigadeTripEntity, Long> {

    @Query(value = """
            SELECT bt FROM BrigadeTripEntity bt
            WHERE bt.brigade.agency = :agency
            AND bt.brigade.calendar IN :calendars""")
    List<BrigadeTripEntity> findAllByAgencyAndActiveCalendars(
            @Param("agency") AgencyEntity agency,
            @Param("calendars") List<CalendarEntity> calendars);
}
