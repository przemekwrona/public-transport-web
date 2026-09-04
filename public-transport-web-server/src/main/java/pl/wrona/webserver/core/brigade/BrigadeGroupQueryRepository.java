package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;

import java.util.List;

@Repository
public interface BrigadeGroupQueryRepository extends JpaRepository<BrigadeGroupEntity, Long> {

    @Query("""
            SELECT b FROM BrigadeGroupEntity b
            WHERE b.calendarSymbol.calendarItem.agency.agencyCode = :instance
            AND b.brigadeItem.brigadeItemCode = :brigadeCode""")
    BrigadeGroupEntity findBtBrigadeCode(@Param("instance") String instance, @Param("brigadeCode") String brigadeCode);

    @Query("""
            SELECT b FROM BrigadeGroupEntity b
            WHERE b.calendarSymbol.calendarItem.agency.agencyCode = :instance
            AND b.brigadeItem.brigadeItemCode = :brigadeCode
            AND b.calendarSymbol.calendarItem.sequenceHex = :calendarCode
            AND b.calendarSymbol.designation = :calendarSymbol""")
    BrigadeGroupEntity findBtBrigadeCode(@Param("instance") String instance, @Param("brigadeCode") String brigadeCode, @Param("calendarCode") String calendarCode, @Param("calendarSymbol") String calendarSymbol);

    @Query("""
            SELECT b FROM BrigadeGroupEntity b
            JOIN FETCH b.brigadeItem
            JOIN FETCH b.calendarSymbol s
            JOIN FETCH s.calendarItem
            WHERE s.calendarItem.agency.agencyCode = :instance""")
    List<BrigadeGroupEntity> findAllByAgencyCode(@Param("instance") String instance);

    @Query("""
            SELECT b FROM BrigadeGroupEntity b
            JOIN FETCH b.brigadeItem i
            JOIN FETCH b.calendarSymbol s
            JOIN FETCH s.calendarItem
            WHERE s.calendarItem.agency.agencyCode = :instance
            AND i.brigadeItemCode = :brigadeCode""")
    List<BrigadeGroupEntity> findAllByAgencyCodeAndBrigadeCode(
            @Param("instance") String instance,
            @Param("brigadeCode") String brigadeCode);

    @Query("""
            SELECT CASE WHEN (COUNT(*) > 0) THEN TRUE ELSE FALSE END
            FROM BrigadeGroupEntity g
            WHERE g.calendarSymbol = :calendarSymbol""")
    boolean existsByCalendarSymbol(@Param("calendarSymbol") CalendarSymbolEntity calendarSymbol);

}
