package pl.wrona.webserver.core.gtfs;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.entity.StopEntity;

public class StopHandler {

    public static Stop handle(AgencyEntity agencyEntity, StopEntity stopEntity) {
        var agencyAndId = new AgencyAndId();
        agencyAndId.setAgencyId(agencyEntity.getAgencyCode());
        agencyAndId.setId(stopEntity.getStopId().toString());

        Stop stop = new Stop();
        stop.setId(agencyAndId);
        stop.setName(stopEntity.getName());
        stop.setLat(stopEntity.getLat());
        stop.setLon(stopEntity.getLon());

        return stop;
    }
}
