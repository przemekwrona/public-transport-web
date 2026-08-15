package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrigadeItemSequenceQueryRepository extends JpaRepository<BrigadeItemSequenceEntity, BrigadeItemSequenceId> {

    @Query("""
            SELECT s FROM BrigadeItemSequenceEntity s
            WHERE s.id.agencyCode = :agencyCode
            AND s.id.calendarCode = :calendarCode""")
    BrigadeItemSequenceEntity findByAgencyCodeAndCalendarCode(
            @Param("agencyCode") String agencyCode,
            @Param("calendarCode") String calendarCode);

}
