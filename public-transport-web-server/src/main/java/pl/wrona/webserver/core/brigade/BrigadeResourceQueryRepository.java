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
        SELECT r FROM BrigadeResourceEntity r JOIN FETCH r.brigadeGroup g
        WHERE g.brigadeItem.agency.agencyCode = :agency
        AND g.brigadeItem.brigadeItemSequence = :brigadeItemSequence
        AND g.calendarSymbol.calendarItem.sequence = :calendarSequence
        AND g.calendarSymbol.designation = :symbol
        AND r.resourceSequence = :resourceSequence""")
    BrigadeResourceEntity findByAgencyAndCalendarAndSymbolAndResourceCode(
            @Param("agency") String agency,
            @Param("brigadeItemSequence") int brigadeItemSequence,
            @Param("calendarSequence") int calendarSequence,
            @Param("symbol") String symbol,
            @Param("resourceSequence") int resourceSequence);
}
