package pl.wrona.webserver.core.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerritorialUnitRepository extends JpaRepository<TerritorialUnitEntity, Long> {

    @Query(value = "SELECT t FROM TerritorialUnitEntity t WHERE t.territorialUnitId IN :territoriesIds")
    List<TerritorialUnitEntity> findAllByTerritoriesIdIn(@Param("territoriesIds") List<Long> territoriesIds);

    @Query(value = "SELECT t FROM TerritorialUnitEntity t JOIN t.busStops s WHERE s.stopId IN :stopIds")
    List<TerritorialUnitEntity> findAllByStopIdIn(@Param("stopIds") List<Long> stopIds);

    List<TerritorialUnitEntity> findAllByNazwaStartingWith(String nazwa);
}
