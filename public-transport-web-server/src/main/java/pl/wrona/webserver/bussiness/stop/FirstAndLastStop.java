package pl.wrona.webserver.bussiness.stop;

import pl.wrona.webserver.core.entity.StopEntity;

public record FirstAndLastStop(StopEntity firstStop, StopEntity lastStop) {
}
