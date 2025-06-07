package pl.wrona.webserver.agency.gtfs;

import org.onebusaway.gtfs.model.Agency;

public class AgencyHandler {

    public static Agency handle(pl.wrona.webserver.agency.entity.Agency agencyEntity) {
        Agency agency = new Agency();
        agency.setId(agencyEntity.getAgencyCode());
        agency.setName(agencyEntity.getAgencyName());
        agency.setUrl("https://www.%s".formatted(agencyEntity.getAgencyUrl()));
        agency.setTimezone("Europe/Warsaw");

        return agency;
    }
}
