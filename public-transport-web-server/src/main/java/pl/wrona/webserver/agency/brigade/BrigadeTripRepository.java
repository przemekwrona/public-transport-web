package pl.wrona.webserver.agency.brigade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrigadeTripRepository extends JpaRepository<BrigadeTripEntity, Long> {

    @Query("SELECT bt FROM BrigadeTripEntity bt WHERE bt.brigade.brigadeNumber = :brigadeName")
    List<BrigadeTripEntity> findAllByBrigadeName(@Param("brigadeName") String brigadeName);

//    @Modifying
//    @Query("DELETE FROM BrigadeTripEntity bt WHERE bt.brigade.brigadeId = :brigade")
//    void deleteAllByBrigade(@Param("brigade") BrigadeEntity brigade);

    @Modifying
    void deleteAllByBrigade(BrigadeEntity brigade);
}
