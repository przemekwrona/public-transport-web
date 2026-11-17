package pl.wrona.webserver.core.stop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface TerytRepository extends JpaRepository<TerytEntity, String> {

    @Query(value = "SELECT t FROM TerytEntity t WHERE t.teryt IN :woj")
    Set<TerytEntity> findTerrytWojewodztwoByWojIn(@Param("woj") Set<String> woj);

    @Query(value = "SELECT t FROM TerytEntity t WHERE t.teryt IN :teryts")
    Set<TerytEntity> findTerrytPowiatByTeryIn(@Param("teryts") Set<String> teryts);

    @Query(value = "SELECT t FROM TerytEntity t WHERE t.idterc IN :territoryIdtercs")
    Set<TerytEntity> findTerrytGminaByIdtercIn(@Param("territoryIdtercs") Set<String> territoryIdtercs);

    /**
     * Gmina rows often have null {@code teryt} (entity {@code @Id}), so load names as a projection.
     */
    @Query("SELECT t.idterc AS idterc, t.nazwa AS nazwa FROM TerytEntity t WHERE t.idterc IN :idtercs")
    List<TerytNameView> findNamesByIdtercIn(@Param("idtercs") Collection<String> idtercs);

    @Query("""
            SELECT t.idterc AS idterc, t.nazwa AS nazwa
            FROM TerytEntity t
            WHERE t.idterc IN :idtercs
               OR SUBSTRING(t.idterc, 1, 6) IN :prefixes
            """)
    List<TerytNameView> findNamesByIdtercInOrPrefixIn(
            @Param("idtercs") Collection<String> idtercs,
            @Param("prefixes") Collection<String> prefixes);

}
