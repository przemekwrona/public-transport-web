package pl.wrona.webserver.bussiness.gtfs.download;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;

public class RouteHandler {

    public static Route handle(AgencyEntity agencyEntity, RouteEntity entity) {
        Route route = new Route();

        var agencyAndId = new AgencyAndId();
        agencyAndId.setAgencyId(agencyEntity.getAgencyCode());
        agencyAndId.setId(entity.getRouteId());

        var agency = new org.onebusaway.gtfs.model.Agency();
        agency.setId(agencyAndId.getAgencyId());

        route.setAgency(agency);
        route.setId(agencyAndId);
        route.setShortName(entity.getLine());
        route.setLongName(entity.getName());
        route.setDesc(entity.getDescription());

        return route;
    }
}
