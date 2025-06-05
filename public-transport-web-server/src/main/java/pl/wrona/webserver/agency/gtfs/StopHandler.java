package pl.wrona.webserver.agency.gtfs;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.agency.entity.StopEntity;

public class StopHandler {

    public static Stop handle(Agency agency, StopEntity stopEntity) {
        var agencyAndId = new AgencyAndId();
        agencyAndId.setAgencyId(agency.getAgencyCode());
        agencyAndId.setId(stopEntity.getStopId().toString());

        Stop stop = new Stop();
        stop.setId(agencyAndId);
        stop.setName(stopEntity.getName());
        stop.setLat(stopEntity.getLat());
        stop.setLon(stopEntity.getLon());

        return stop;
    }
}
