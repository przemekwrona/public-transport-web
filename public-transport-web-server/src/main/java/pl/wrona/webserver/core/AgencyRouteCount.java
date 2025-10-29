package pl.wrona.webserver.core;

import pl.wrona.webserver.core.agency.AgencyEntity;

public interface AgencyRouteCount {
    AgencyEntity getAgency();
    Integer getRouteCount();
}
