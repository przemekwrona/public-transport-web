package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrigadeEventSequenceQueryRepository extends JpaRepository<BrigadeEventSequenceEntity, BrigadeEventSequenceId> {

    @Query("""
            SELECT s FROM BrigadeEventSequenceEntity s
            WHERE s.id.agencyCode = :agencyCode
            AND s.id.calendarCode = :calendarCode
            AND s.id.calendarSymbol = :calendarSymbol""")
    BrigadeEventSequenceEntity findByAgencyCodeAndCalendarCodeAndCalendarSymbol(
            @Param("agencyCode") String agencyCode,
            @Param("calendarCode") String calendarCode,
            @Param("calendarSymbol") String calendarSymbol);

}
