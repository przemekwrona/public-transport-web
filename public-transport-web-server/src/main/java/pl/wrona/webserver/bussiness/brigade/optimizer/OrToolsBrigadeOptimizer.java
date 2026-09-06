package pl.wrona.webserver.bussiness.brigade.optimizer;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.LocalSearchMetaheuristic;
import com.google.ortools.constraintsolver.RoutingDimension;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.main;
import com.google.protobuf.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OrToolsBrigadeOptimizer {

    private static final long SOLVE_TIME_LIMIT_SECONDS = 5;
    private static final long DROP_TRIP_PENALTY = 1_000_000L;

    static {
        Loader.loadNativeLibraries();
    }

    public record TripRequest(int fromLoc, int toLoc, long startTime, long endTime) {
    }

    /**
     * Assigns trips to vehicles. A brigade may start at its first trip and finish at its last
     * trip; there is no pull-out or pull-in to a depot. Returns one list of trip indexes per
     * vehicle, in visit order. Trips that do not fit on the available vehicles are left
     * unassigned. Returns an empty list when there is nothing to assign.
     */
    public List<List<Integer>> assignTrips(long[][] physicalTimeMatrix,
                                           List<TripRequest> requestedTrips,
                                           int numVehicles) {
        if (physicalTimeMatrix == null || requestedTrips == null || requestedTrips.isEmpty() || numVehicles < 1) {
            return List.of();
        }

        int tripCount = requestedTrips.size();
        int totalNodes = tripCount + 1;
        long[][] timeTransit = new long[totalNodes][totalNodes];
        long[][] costTransit = new long[totalNodes][totalNodes];
        fillTransitMatrices(physicalTimeMatrix, requestedTrips, timeTransit, costTransit);
        long horizon = horizon(requestedTrips, timeTransit);

        RoutingIndexManager manager = new RoutingIndexManager(totalNodes, numVehicles, 0);
        RoutingModel routing = new RoutingModel(manager);
        try {
            int timeCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) ->
                    timeTransit[manager.indexToNode(fromIndex)][manager.indexToNode(toIndex)]);
            int costCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) ->
                    costTransit[manager.indexToNode(fromIndex)][manager.indexToNode(toIndex)]);

            routing.setArcCostEvaluatorOfAllVehicles(costCallbackIndex);
            routing.addDimension(timeCallbackIndex, horizon, horizon, false, "Time");
            RoutingDimension timeDimension = routing.getMutableDimension("Time");

            for (int vehicle = 0; vehicle < numVehicles; vehicle++) {
                timeDimension.cumulVar(routing.start(vehicle)).setRange(0, horizon);
                timeDimension.cumulVar(routing.end(vehicle)).setRange(0, horizon);
            }
            for (int tripIndex = 0; tripIndex < tripCount; tripIndex++) {
                long startTime = requestedTrips.get(tripIndex).startTime();
                long tripIndexInModel = manager.nodeToIndex(tripIndex + 1);
                timeDimension.cumulVar(tripIndexInModel).setRange(startTime, startTime);
                routing.addDisjunction(new long[]{tripIndexInModel}, DROP_TRIP_PENALTY);
            }

            List<List<Integer>> seedRoutes = greedyAssignment(physicalTimeMatrix, requestedTrips, numVehicles);
            Assignment solution = improveFromSeed(routing, seedRoutes);
            if (solution == null) {
                return copyRoutes(seedRoutes);
            }

            List<List<Integer>> tripsByVehicle = new ArrayList<>(numVehicles);
            for (int vehicle = 0; vehicle < numVehicles; vehicle++) {
                List<Integer> vehicleTrips = new ArrayList<>();
                long index = routing.start(vehicle);
                while (!routing.isEnd(index)) {
                    int node = manager.indexToNode(index);
                    if (node > 0) {
                        vehicleTrips.add(node - 1);
                    }
                    index = solution.value(routing.nextVar(index));
                }
                tripsByVehicle.add(List.copyOf(vehicleTrips));
            }
            return List.copyOf(tripsByVehicle);
        } finally {
            routing.delete();
            manager.delete();
        }
    }

    private static void fillTransitMatrices(long[][] physicalTimeMatrix,
                                            List<TripRequest> requestedTrips,
                                            long[][] timeTransit,
                                            long[][] costTransit) {
        int totalNodes = requestedTrips.size() + 1;
        for (int fromNode = 0; fromNode < totalNodes; fromNode++) {
            boolean fromDummyStart = fromNode == 0;
            long serviceTime = fromDummyStart
                    ? 0L
                    : requestedTrips.get(fromNode - 1).endTime() - requestedTrips.get(fromNode - 1).startTime();
            for (int toNode = 0; toNode < totalNodes; toNode++) {
                if (fromNode == toNode) {
                    continue;
                }
                boolean toDummyEnd = toNode == 0;
                if (fromDummyStart || toDummyEnd) {
                    timeTransit[fromNode][toNode] = serviceTime;
                    continue;
                }
                int fromLoc = requestedTrips.get(fromNode - 1).toLoc();
                int toLoc = requestedTrips.get(toNode - 1).fromLoc();
                long deadhead = physicalTimeMatrix[fromLoc][toLoc];
                timeTransit[fromNode][toNode] = serviceTime + deadhead;
                costTransit[fromNode][toNode] = deadhead;
            }
        }
    }

    private static Assignment improveFromSeed(RoutingModel routing, List<List<Integer>> seedRoutes) {
        RoutingSearchParameters parameters = searchParameters();
        routing.closeModelWithParameters(parameters);
        Assignment seed = routing.readAssignmentFromRoutes(toNodeRoutes(seedRoutes), true);
        if (seed == null) {
            return null;
        }
        Assignment improved = routing.solveFromAssignmentWithParameters(seed, parameters);
        return improved != null ? improved : seed;
    }

    private static List<List<Integer>> greedyAssignment(long[][] physicalTimeMatrix,
                                                        List<TripRequest> requestedTrips,
                                                        int numVehicles) {
        List<Integer> tripOrder = new ArrayList<>(requestedTrips.size());
        for (int tripIndex = 0; tripIndex < requestedTrips.size(); tripIndex++) {
            tripOrder.add(tripIndex);
        }
        tripOrder.sort(Comparator.comparingLong(tripIndex -> requestedTrips.get(tripIndex).startTime()));

        List<List<Integer>> routes = new ArrayList<>(numVehicles);
        for (int vehicle = 0; vehicle < numVehicles; vehicle++) {
            routes.add(new ArrayList<>());
        }

        for (int tripIndex : tripOrder) {
            TripRequest trip = requestedTrips.get(tripIndex);
            int bestVehicle = -1;
            long bestDeadhead = Long.MAX_VALUE;
            long bestWait = Long.MAX_VALUE;

            for (int vehicle = 0; vehicle < numVehicles; vehicle++) {
                List<Integer> route = routes.get(vehicle);
                if (route.isEmpty()) {
                    continue;
                }
                TripRequest last = requestedTrips.get(route.get(route.size() - 1));
                long deadhead = physicalTimeMatrix[last.toLoc()][trip.fromLoc()];
                long arrival = last.endTime() + deadhead;
                if (arrival > trip.startTime()) {
                    continue;
                }
                long wait = trip.startTime() - arrival;
                if (deadhead < bestDeadhead || (deadhead == bestDeadhead && wait < bestWait)) {
                    bestVehicle = vehicle;
                    bestDeadhead = deadhead;
                    bestWait = wait;
                }
            }

            if (bestVehicle < 0) {
                for (int vehicle = 0; vehicle < numVehicles; vehicle++) {
                    if (routes.get(vehicle).isEmpty()) {
                        bestVehicle = vehicle;
                        break;
                    }
                }
            }
            if (bestVehicle >= 0) {
                routes.get(bestVehicle).add(tripIndex);
            }
        }
        return routes;
    }

    private static List<List<Integer>> copyRoutes(List<List<Integer>> routes) {
        List<List<Integer>> copy = new ArrayList<>(routes.size());
        for (List<Integer> route : routes) {
            copy.add(List.copyOf(route));
        }
        return List.copyOf(copy);
    }

    private static long[][] toNodeRoutes(List<List<Integer>> tripsByVehicle) {
        long[][] routes = new long[tripsByVehicle.size()][];
        for (int vehicle = 0; vehicle < tripsByVehicle.size(); vehicle++) {
            List<Integer> trips = tripsByVehicle.get(vehicle);
            long[] nodes = new long[trips.size()];
            for (int i = 0; i < trips.size(); i++) {
                nodes[i] = trips.get(i) + 1L;
            }
            routes[vehicle] = nodes;
        }
        return routes;
    }

    private static RoutingSearchParameters searchParameters() {
        return main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GREEDY_DESCENT)
                .setTimeLimit(Duration.newBuilder().setSeconds(SOLVE_TIME_LIMIT_SECONDS).build())
                .build();
    }

    private static long horizon(List<TripRequest> requestedTrips, long[][] timeTransit) {
        long latestTripEnd = requestedTrips.stream()
                .mapToLong(TripRequest::endTime)
                .max()
                .orElse(0L);
        long maxTransit = 0L;
        for (long[] row : timeTransit) {
            for (long travelTime : row) {
                maxTransit = Math.max(maxTransit, travelTime);
            }
        }
        return latestTripEnd + maxTransit;
    }
}
