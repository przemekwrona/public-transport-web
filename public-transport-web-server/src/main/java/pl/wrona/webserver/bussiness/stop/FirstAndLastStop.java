package pl.wrona.webserver.bussiness.stop;

import pl.wrona.webserver.core.stop.StopEntity;

public record FirstAndLastStop(StopEntity firstStop, StopEntity lastStop) {
}
