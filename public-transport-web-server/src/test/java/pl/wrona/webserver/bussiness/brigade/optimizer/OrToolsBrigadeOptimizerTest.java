package pl.wrona.webserver.bussiness.brigade.optimizer;

import org.junit.jupiter.api.Test;
import pl.wrona.webserver.bussiness.brigade.optimizer.OrToolsBrigadeOptimizer.TripRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrToolsBrigadeOptimizerTest {

    private final OrToolsBrigadeOptimizer optimizer = new OrToolsBrigadeOptimizer();

    @Test
    void assignsScheduledTripsToBrigades() {
        long[][] physicalTimeMatrix = {
                {0, 10, 15},
                {10, 0, 20},
                {15, 20, 0}
        };
        List<TripRequest> requestedTrips = List.of(
                new TripRequest(1, 2, 480, 510),
                new TripRequest(1, 2, 540, 570),
                new TripRequest(2, 1, 600, 630));

        long startedAt = System.nanoTime();
        List<List<Integer>> tripsByVehicle = optimizer.assignTrips(physicalTimeMatrix, requestedTrips, 2);
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        assertThat(elapsedMs).isLessThan(2_000);
        assertThat(tripsByVehicle).hasSize(2);
        assertAllTripsAssignedOnce(tripsByVehicle, requestedTrips.size());
        assertRoutesAreTimeFeasible(tripsByVehicle, requestedTrips, physicalTimeMatrix);
    }

    @Test
    void assignsAFullServiceDayWithinTwoSeconds() {
        int locations = 6;
        long[][] physicalTimeMatrix = new long[locations][locations];
        for (int from = 0; from < locations; from++) {
            for (int to = 0; to < locations; to++) {
                physicalTimeMatrix[from][to] = from == to ? 0L : 8L;
            }
        }

        int vehicles = 8;
        int tripsPerVehicle = 12;
        List<TripRequest> requestedTrips = new ArrayList<>();
        for (int vehicle = 0; vehicle < vehicles; vehicle++) {
            for (int trip = 0; trip < tripsPerVehicle; trip++) {
                int fromLoc = (vehicle + trip) % locations;
                int toLoc = (fromLoc + 1) % locations;
                long startTime = 5 * 3600L + vehicle * 180L + trip * 1800L;
                requestedTrips.add(new TripRequest(fromLoc, toLoc, startTime, startTime + 900L));
            }
        }

        long startedAt = System.nanoTime();
        List<List<Integer>> tripsByVehicle = optimizer.assignTrips(physicalTimeMatrix, requestedTrips, vehicles);
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        assertThat(elapsedMs).isLessThan(2_000);
        assertThat(tripsByVehicle).hasSize(vehicles);
        assertAllTripsAssignedOnce(tripsByVehicle, requestedTrips.size());
        assertRoutesAreTimeFeasible(tripsByVehicle, requestedTrips, physicalTimeMatrix);
    }

    @Test
    void assignsWhatFitsWhenFewerVehiclesThanNeeded() {
        int locations = 6;
        long[][] physicalTimeMatrix = new long[locations][locations];
        for (int from = 0; from < locations; from++) {
            for (int to = 0; to < locations; to++) {
                physicalTimeMatrix[from][to] = from == to ? 0L : 8L;
            }
        }

        int availableVehicles = 5;
        List<TripRequest> requestedTrips = new ArrayList<>();
        for (int trip = 0; trip < 86; trip++) {
            int fromLoc = trip % locations;
            int toLoc = (fromLoc + 1) % locations;
            long startTime = 6 * 3600L + trip * 180L;
            requestedTrips.add(new TripRequest(fromLoc, toLoc, startTime, startTime + 1800L));
        }

        long startedAt = System.nanoTime();
        List<List<Integer>> tripsByVehicle = optimizer.assignTrips(
                physicalTimeMatrix, requestedTrips, availableVehicles);
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        int assigned = tripsByVehicle.stream().mapToInt(List::size).sum();
        assertThat(elapsedMs).isLessThan(2_000);
        assertThat(tripsByVehicle).hasSize(availableVehicles);
        assertThat(assigned).isGreaterThan(0);
        assertThat(assigned).isLessThan(requestedTrips.size());
        assertRoutesAreTimeFeasible(tripsByVehicle, requestedTrips, physicalTimeMatrix);
    }

    @Test
    void keepsReachableTripsWhenTheNextTripIsTooFar() {
        long[][] physicalTimeMatrix = {
                {0, 0, 0},
                {0, 0, 40},
                {0, 40, 0}
        };
        List<TripRequest> requestedTrips = List.of(
                new TripRequest(1, 2, 480, 510),
                new TripRequest(1, 2, 520, 550));

        long startedAt = System.nanoTime();
        List<List<Integer>> tripsByVehicle = optimizer.assignTrips(physicalTimeMatrix, requestedTrips, 1);
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        assertThat(elapsedMs).isLessThan(2_000);
        assertThat(tripsByVehicle).hasSize(1);
        assertThat(tripsByVehicle.get(0)).hasSize(1);
        assertRoutesAreTimeFeasible(tripsByVehicle, requestedTrips, physicalTimeMatrix);
    }

    @Test
    void canStartAtFirstTripWithoutTravelFromDepot() {
        long[][] physicalTimeMatrix = {
                {0, 15, 15},
                {15, 0, 20},
                {15, 20, 0}
        };
        List<TripRequest> requestedTrips = List.of(new TripRequest(1, 2, 5, 30));

        List<List<Integer>> tripsByVehicle = optimizer.assignTrips(physicalTimeMatrix, requestedTrips, 1);

        assertThat(tripsByVehicle).hasSize(1);
        assertThat(tripsByVehicle.get(0)).containsExactly(0);
    }

    @Test
    void returnsEmptyWhenThereAreNoTrips() {
        assertThat(optimizer.assignTrips(new long[][]{{0}}, List.of(), 1)).isEmpty();
    }

    private static void assertAllTripsAssignedOnce(List<List<Integer>> tripsByVehicle, int tripCount) {
        List<Integer> assigned = tripsByVehicle.stream().flatMap(List::stream).toList();
        assertThat(assigned).hasSize(tripCount);
        assertThat(new HashSet<>(assigned)).isEqualTo(range(tripCount));
    }

    private static void assertRoutesAreTimeFeasible(List<List<Integer>> tripsByVehicle,
                                                    List<TripRequest> requestedTrips,
                                                    long[][] physicalTimeMatrix) {
        for (List<Integer> vehicleTrips : tripsByVehicle) {
            Integer previousLocation = null;
            long previousEndTime = 0L;
            for (int tripIndex : vehicleTrips) {
                TripRequest trip = requestedTrips.get(tripIndex);
                if (previousLocation != null) {
                    long arrival = previousEndTime + physicalTimeMatrix[previousLocation][trip.fromLoc()];
                    assertThat(arrival).isLessThanOrEqualTo(trip.startTime());
                }
                previousLocation = trip.toLoc();
                previousEndTime = trip.endTime();
            }
        }
    }

    private static Set<Integer> range(int tripCount) {
        Set<Integer> indexes = new HashSet<>();
        for (int index = 0; index < tripCount; index++) {
            indexes.add(index);
        }
        return indexes;
    }
}
