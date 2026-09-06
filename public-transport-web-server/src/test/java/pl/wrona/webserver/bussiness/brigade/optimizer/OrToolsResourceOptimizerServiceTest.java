package pl.wrona.webserver.bussiness.brigade.optimizer;

import org.junit.jupiter.api.Test;
import pl.wrona.webserver.bussiness.brigade.optimizer.OrToolsBrigadeOptimizer.TripRequest;
import pl.wrona.webserver.bussiness.stop.FirstAndLastStop;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.entity.StopEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OrToolsResourceOptimizerServiceTest {

    @Test
    void buildProblemMapsEventsToPickupAndDeliveryTrips() {
        var outbound = profile(10L);
        var inbound = profile(11L);
        var stopA = stop(100L, 52.2300, 21.0100);
        var stopB = stop(200L, 52.2310, 21.0120);
        var firstTrip = event(outbound, 8 * 3600, 8 * 3600 + 1800);
        var secondTrip = event(inbound, 10 * 3600, 10 * 3600 + 1800);

        var problem = OrToolsResourceOptimizerService.buildProblem(
                List.of(firstTrip, secondTrip),
                Map.of(
                        outbound, new FirstAndLastStop(stopA, stopB),
                        inbound, new FirstAndLastStop(stopB, stopA)));

        assertThat(problem.events()).containsExactly(firstTrip, secondTrip);
        assertThat(problem.trips()).containsExactly(
                new TripRequest(0, 1, 8 * 3600, 8 * 3600 + 1800),
                new TripRequest(1, 0, 10 * 3600, 10 * 3600 + 1800));
        assertThat(problem.timeMatrix()).hasDimensions(2, 2);
        assertThat(problem.timeMatrix()[0][1]).isPositive();
        assertThat(problem.timeMatrix()[0][0]).isZero();
    }

    @Test
    void applyAssignmentMovesEventsOntoSolvedResources() {
        var firstResource = resource(1L);
        var secondResource = resource(2L);
        var firstTrip = event(profile(10L), 100, 200);
        var secondTrip = event(profile(11L), 300, 400);
        firstTrip.setResource(secondResource);
        secondTrip.setResource(firstResource);

        OrToolsResourceOptimizerService.applyAssignment(
                List.of(firstResource, secondResource),
                List.of(firstTrip, secondTrip),
                List.of(List.of(1), List.of(0)));

        assertThat(firstTrip.getResource()).isSameAs(secondResource);
        assertThat(secondTrip.getResource()).isSameAs(firstResource);
    }

    private static BrigadeResourceEntity resource(Long id) {
        var resource = new BrigadeResourceEntity();
        resource.setBrigadeResourceId(id);
        return resource;
    }

    private static TripProfileEntity profile(Long id) {
        var profile = new TripProfileEntity();
        profile.setTripProfileId(id);
        return profile;
    }

    private static StopEntity stop(Long id, double lat, double lon) {
        var stop = new StopEntity();
        stop.setStopId(id);
        stop.setLat(lat);
        stop.setLon(lon);
        return stop;
    }

    private static BrigadeEventEntity event(TripProfileEntity profile, int startSecond, int endSecond) {
        var event = new BrigadeEventEntity();
        event.setTripProfile(profile);
        event.setStartSecond(startSecond);
        event.setEndSecond(endSecond);
        return event;
    }
}
