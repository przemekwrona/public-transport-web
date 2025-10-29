package pl.wrona.webserver.bussiness.gtfs.download;

import org.onebusaway.gtfs.model.Agency;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;

public class AgencyHandler {

    public static Agency handle(AgencyEntity agencyEntity) {
        Agency agency = new Agency();
        agency.setId(agencyEntity.getAgencyCode());
        agency.setName(agencyEntity.getAgencyName());
        agency.setUrl("https://www.%s".formatted(agencyEntity.getAgencyUrl()));
        agency.setTimezone("Europe/Warsaw");

        return agency;
    }
}
