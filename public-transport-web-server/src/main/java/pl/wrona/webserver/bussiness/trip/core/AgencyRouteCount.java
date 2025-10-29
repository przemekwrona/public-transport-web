package pl.wrona.webserver.bussiness.trip.core;

import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;

public interface AgencyRouteCount {
    AgencyEntity getAgency();
    Integer getRouteCount();
}
