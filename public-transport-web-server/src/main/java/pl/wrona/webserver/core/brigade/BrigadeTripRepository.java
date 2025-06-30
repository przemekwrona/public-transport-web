package pl.wrona.webserver.core.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.entity.Agency;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BrigadeTripRepository extends JpaRepository<BrigadeTripEntity, Long> {

    @Query("SELECT bt FROM BrigadeTripEntity bt WHERE bt.brigade.brigadeNumber = :brigadeName")
    List<BrigadeTripEntity> findAllByBrigadeName(@Param("brigadeName") String brigadeName);

    @Query("SELECT bt FROM BrigadeTripEntity bt WHERE bt.brigade.agency = :agency AND bt.brigade.calendar.startDate <= :date AND bt.brigade.calendar.endDate >= :date")
    List<BrigadeTripEntity> findAllByAgencyAndActiveCalendar(@Param("agency") Agency agency, @Param("date") LocalDate date);

    @Modifying
    void deleteAllByBrigade(BrigadeEntity brigade);
}
