package pl.wrona.webserver.bussiness.route.stop.sequencer;

import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.RouteStopGraph;
import org.igeolab.iot.pt.server.api.model.RouteStopGraphBranch;
import org.igeolab.iot.pt.server.api.model.RouteStopGraphBranchType;
import org.igeolab.iot.pt.server.api.model.RouteStopGraphEdge;
import org.igeolab.iot.pt.server.api.model.RouteStopGraphNode;
import org.igeolab.iot.pt.server.api.model.RouteStopGraphNodeRole;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;

@UtilityClass
public class RouteStopGraphMapper {

    public RouteStopGraph map(StopGraph graph) {
        if (graph == null) {
            return null;
        }
        return new RouteStopGraph()
                .mode(TripVariantModeMapper.map(graph.mode()))
                .nodes(graph.nodes().stream().map(RouteStopGraphMapper::map).toList())
                .edges(graph.edges().stream().map(RouteStopGraphMapper::map).toList())
                .spineStopIds(graph.spineStopIds())
                .spineTripCode(graph.spineTripCode())
                .branches(graph.branches().stream().map(RouteStopGraphMapper::map).toList());
    }

    private RouteStopGraphNode map(StopGraph.Node node) {
        return new RouteStopGraphNode()
                .stopId(node.stopId())
                .name(node.name())
                .lat(node.lat())
                .lon(node.lon())
                .inDegree(node.inDegree())
                .outDegree(node.outDegree())
                .layer(node.layer())
                .roles(node.roles().stream()
                        .map(role -> RouteStopGraphNodeRole.fromValue(role.name()))
                        .toList());
    }

    private RouteStopGraphEdge map(StopGraph.Edge edge) {
        return new RouteStopGraphEdge()
                .fromStopId(edge.fromStopId())
                .toStopId(edge.toStopId())
                .tripCodes(edge.tripCodes());
    }

    private RouteStopGraphBranch map(StopGraph.Branch branch) {
        return new RouteStopGraphBranch()
                .tripCode(branch.tripCode())
                .variantName(branch.variantName())
                .variantDesignation(branch.variantDesignation())
                .type(RouteStopGraphBranchType.fromValue(branch.type().name()))
                .divergeFromStopId(branch.divergeFromStopId())
                .rejoinAtStopId(branch.rejoinAtStopId())
                .stopIds(branch.stopIds());
    }
}
