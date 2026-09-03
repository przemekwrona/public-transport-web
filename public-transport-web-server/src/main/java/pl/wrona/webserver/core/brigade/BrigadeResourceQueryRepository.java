package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrigadeResourceQueryRepository extends JpaRepository<BrigadeResourceEntity, Long> {

    List<BrigadeResourceEntity> findAllByBrigadeGroupBrigadeGroupIdOrderByResourceSequenceAsc(Long brigadeGroupId);

    @Query("""
            SELECT r FROM BrigadeResourceEntity r
            WHERE r.brigadeGroup.calendarSymbol.calendarItem.agency.agencyCode = :agency
            AND r.brigadeGroup.calendarSymbol.calendarItem.sequenceHex = :calendarCode
            AND r.brigadeGroup.calendarSymbol.designation = :symbol
            AND r.resourceCode = :resourceCode""")
    BrigadeResourceEntity findByAgencyAndCalendarAndSymbolAndResourceCode(
            @Param("agency") String agency,
            @Param("calendarCode") String calendarCode,
            @Param("symbol") String symbol,
            @Param("resourceCode") String resourceCode);
}
