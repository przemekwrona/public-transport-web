package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrigadeGroupQueryRepository extends JpaRepository<BrigadeGroupEntity, Long> {

    @Query("""
            SELECT b FROM BrigadeGroupEntity b
            WHERE b.calendarSymbol.calendarItem.agency.agencyCode = :instance
            AND b.calendarSymbol.calendarItem.sequenceHex = :calendarCode
            AND b.calendarSymbol.designation = :calendarSymbol""")
    BrigadeGroupEntity findByCalendarSymbol(@Param("instance") String instance, @Param("calendarCode") String calendarCode, @Param("calendarSymbol") String calendarSymbol);

    @Query("""
            SELECT b FROM BrigadeGroupEntity b JOIN b.calendarSymbol s JOIN s.calendarItem
            WHERE b.calendarSymbol.calendarItem.agency.agencyCode = :instance""")
    List<BrigadeGroupEntity> findAllByAgencyCode(@Param("instance") String instance);

}
