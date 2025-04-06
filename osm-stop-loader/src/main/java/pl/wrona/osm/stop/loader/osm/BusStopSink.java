package pl.wrona.osm.stop.loader.osm;

import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.springframework.stereotype.Component;
import pl.wrona.osm.stop.loader.StopResource;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BusStopSink implements Sink {
    private final Tag BUS_STOP_TAG = new Tag("public_transport", "platform");
    private final Tag BUS_STOP_TAG_V2 = new Tag("highway", "bus_stop");

    private final long startTime;
    private final StopResource stopResource;

    public BusStopSink(final StopResource stopResource) {
        this.startTime = System.currentTimeMillis();
        this.stopResource = stopResource;
    }

    @Override
    public void process(EntityContainer entityContainer) {
        Entity entity = entityContainer.getEntity();

        if (entity instanceof Node node
                && !entity.getTags().isEmpty()
                && entity.getTags().stream()
                .anyMatch((Tag tag) -> isBusStop(tag, BUS_STOP_TAG) || isBusStop(tag, BUS_STOP_TAG_V2))) {

            this.stopResource.save(node);
        }
    }

    private boolean isBusStop(Tag tag, Tag referenceTag) {
        return tag.getKey().equals(referenceTag.getKey()) && tag.getValue().equals(referenceTag.getValue());
    }

    @Override
    public void initialize(Map<String, Object> metaData) {
        log.info("Initializing OSM stop loader");
    }

    @Override
    public void complete() {
        long endTime = System.nanoTime();
        long elapsedTime = TimeUnit.NANOSECONDS.toSeconds(endTime - startTime);
        log.info("Finished loading OSM stops in {}s", elapsedTime);
    }

    @Override
    public void close() {
        log.info("Closing OSM stop loader");
    }
}
