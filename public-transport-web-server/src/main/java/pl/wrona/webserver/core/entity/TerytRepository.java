package pl.wrona.webserver.core.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TerytRepository extends JpaRepository<TerytEntity, String> {

    @Query(value = "SELECT t FROM TerytEntity t WHERE t.teryt IN :woj")
    Set<TerytEntity> findTerrytWojewodztwoByWojIn(@Param("woj") Set<String> woj);

    @Query(value = "SELECT t FROM TerytEntity t WHERE t.teryt IN :teryts")
    Set<TerytEntity> findTerrytPowiatByTeryIn(@Param("teryts") Set<String> teryts);

    @Query(value = "SELECT t FROM TerytEntity t WHERE t.idterc IN :territoryIdtercs")
    Set<TerytEntity> findTerrytGminaByIdtercIn(@Param("territoryIdtercs") Set<String> territoryIdtercs);

}
