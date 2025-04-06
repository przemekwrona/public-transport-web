package pl.wrona.osm.stop.loader;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@AllArgsConstructor
public class StopResource {

    public static final String INSERT_QUERY = "INSERT INTO stop (bdot10k_id, name, lon, lat) VALUES ('%s', '%s', %f, %f) ON CONFLICT DO NOTHING";
    private static final String TAG_NAME = "name";
    private static final String EMPTY = "";


    private final JdbcTemplate jdbcTemplate;

    public void save(String bdot10kId, String name, double lat, double lon) {
        String query = INSERT_QUERY.formatted(bdot10kId, name, lat, lon);
        try {
            jdbcTemplate.update(query);
        } catch (BadSqlGrammarException ex) {
            log.error("Wrong SQL syntax {}", query);
        }

    }

    public void save(Node node) {
        String stopName = getTagName(node);
        String formatedStopName = stopName.replace("'", "''");

        save(Long.toString(node.getId()), formatedStopName, node.getLatitude(), node.getLongitude());
    }

    private static String getTagName(Node node) {
        return node.getTags().stream().filter(tag -> tag.getKey().equals(TAG_NAME)).findFirst().map(Tag::getValue).orElse(EMPTY);
    }
}
