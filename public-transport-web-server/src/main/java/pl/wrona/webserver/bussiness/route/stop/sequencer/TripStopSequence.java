package pl.wrona.webserver.bussiness.route.stop.sequencer;

import java.util.List;

public record TripStopSequence(
        Long tripId,
        String tripCode,
        String variantName,
        String variantDesignation,
        boolean mainVariant,
        List<StopRef> stops
) {

    public TripStopSequence {
        stops = stops == null ? List.of() : List.copyOf(stops);
    }

    public List<Long> stopIds() {
        return stops.stream().map(StopRef::stopId).toList();
    }
}
