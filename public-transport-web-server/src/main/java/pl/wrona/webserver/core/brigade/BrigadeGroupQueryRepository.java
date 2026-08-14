package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;

@Repository
public interface BrigadeGroupQueryRepository extends JpaRepository<BrigadeGroupEntity, Long> {

    BrigadeGroupEntity findFirstByCalendarSymbol(CalendarSymbolEntity calendarSymbol);

}
