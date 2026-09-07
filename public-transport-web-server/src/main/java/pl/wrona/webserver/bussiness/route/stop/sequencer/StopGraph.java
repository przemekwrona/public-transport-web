package pl.wrona.webserver.bussiness.route.stop.sequencer;

import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;
import java.util.Set;

public record StopGraph(
        TripVariantMode mode,
        List<Node> nodes,
        List<Edge> edges,
        List<Long> spineStopIds,
        String spineTripCode,
        List<Branch> branches
) {

    public StopGraph {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
        spineStopIds = spineStopIds == null ? List.of() : List.copyOf(spineStopIds);
        branches = branches == null ? List.of() : List.copyOf(branches);
    }

    public static StopGraph empty(TripVariantMode mode) {
        return new StopGraph(mode, List.of(), List.of(), List.of(), null, List.of());
    }

    public enum NodeRole {
        ORIGIN,
        DESTINATION,
        FORK,
        JOIN,
        TRUNK,
        ISOLATED
    }

    public enum BranchType {
        DETOUR,
        SHORTCUT,
        EXTENSION,
        DIFFERENT_ORIGIN,
        DIFFERENT_DESTINATION,
        ALTERNATE
    }

    public record Node(
            Long stopId,
            String name,
            double lat,
            double lon,
            int inDegree,
            int outDegree,
            int layer,
            Set<NodeRole> roles
    ) {
        public Node {
            roles = roles == null ? Set.of() : Set.copyOf(roles);
        }
    }

    public record Edge(Long fromStopId, Long toStopId, List<String> tripCodes) {
        public Edge {
            tripCodes = tripCodes == null ? List.of() : List.copyOf(tripCodes);
        }
    }

    public record Branch(
            String tripCode,
            String variantName,
            String variantDesignation,
            BranchType type,
            Long divergeFromStopId,
            Long rejoinAtStopId,
            List<Long> stopIds
    ) {
        public Branch {
            stopIds = stopIds == null ? List.of() : List.copyOf(stopIds);
        }
    }
}
