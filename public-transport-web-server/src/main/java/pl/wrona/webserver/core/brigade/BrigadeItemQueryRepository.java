package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrigadeItemQueryRepository extends JpaRepository<BrigadeItemEntity, Long> {

    @Query("""
            SELECT i FROM BrigadeItemEntity i
            JOIN FETCH i.calendarItem c
            WHERE c.agency.agencyCode = :instance
            AND i.sequenceHex = :brigadeCode""")
    BrigadeItemEntity findByAgencyCodeAndSequenceHex(
            @Param("instance") String instance,
            @Param("brigadeCode") String brigadeCode);

    @Query("""
            SELECT i FROM BrigadeItemEntity i
            JOIN FETCH i.calendarItem c
            WHERE c.agency.agencyCode = :instance
            ORDER BY i.sequence""")
    List<BrigadeItemEntity> findAllByAgencyCode(@Param("instance") String instance);

}
