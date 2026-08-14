package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrigadeResourceSequenceQueryRepository extends JpaRepository<BrigadeResourceSequenceEntity, BrigadeResourceSequenceId> {

    @Query("""
            SELECT s FROM BrigadeResourceSequenceEntity s
            WHERE s.id.agencyCode = :agencyCode
            AND s.id.calendarItemSequence = :calendarItemSequence
            AND s.id.calendarSymbol = :calendarSymbol""")
    BrigadeResourceSequenceEntity findByAgencyCodeAndCalendarItemSequenceAndCalendarSymbol(
            @Param("agencyCode") String agencyCode,
            @Param("calendarItemSequence") Integer calendarItemSequence,
            @Param("calendarSymbol") String calendarSymbol);

}
