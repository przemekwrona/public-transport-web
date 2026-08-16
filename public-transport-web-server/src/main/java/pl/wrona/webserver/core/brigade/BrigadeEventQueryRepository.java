package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BrigadeEventQueryRepository extends JpaRepository<BrigadeEventEntity, Long> {

    List<BrigadeEventEntity> findAllByResourceBrigadeResourceIdInOrderByStartSecondAsc(Collection<Long> resourceIds);

    @Query("""
            SELECT e FROM BrigadeEventEntity e
            WHERE e.resource.brigadeGroup.calendarSymbol.calendarItem.agency.agencyCode = :agency
            AND e.resource.brigadeGroup.brigadeItem.sequenceHex = :brigadeCode
            AND e.resource.brigadeGroup.calendarSymbol.calendarItem.sequenceHex = :calendarCode
            AND e.resource.brigadeGroup.calendarSymbol.designation = :calendarSymbol
            ORDER BY e.startSecond ASC""")
    List<BrigadeEventEntity> findAllByAgencyAndBrigadeAndCalendarAndSymbol(
            @Param("agency") String agency,
            @Param("brigadeCode") String brigadeCode,
            @Param("calendarCode") String calendarCode,
            @Param("calendarSymbol") String calendarSymbol);

    @Query("""
            SELECT e FROM BrigadeEventEntity e
            WHERE e.resource.brigadeGroup.calendarSymbol.calendarItem.agency.agencyCode = :agency
            AND e.resource.brigadeGroup.calendarSymbol.calendarItem.sequenceHex = :calendarCode
            AND e.resource.brigadeGroup.calendarSymbol.designation = :symbol
            AND e.sequenceHex = :eventCode""")
    BrigadeEventEntity findByAgencyAndCalendarAndSymbolAndEventHex(
            @Param("agency") String agency,
            @Param("calendarCode") String calendarCode,
            @Param("symbol") String symbol,
            @Param("eventCode") String eventCode);
}
