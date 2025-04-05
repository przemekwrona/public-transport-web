package pl.wrona.webserver.agency.gtfs;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.agency.entity.RouteEntity;

public class RouteHandler {

    public static Route handle(Agency agency, RouteEntity entity) {
        Route route = new Route();

        var agencyAndId = new AgencyAndId();
        agencyAndId.setAgencyId(agency.getAgencyCode());
        agencyAndId.setId(entity.getLine());

        route.setId(agencyAndId);
        route.setShortName(entity.getLine());
        route.setLongName(entity.getName());
        route.setDesc(entity.getDescription());

        return route;
    }
}
