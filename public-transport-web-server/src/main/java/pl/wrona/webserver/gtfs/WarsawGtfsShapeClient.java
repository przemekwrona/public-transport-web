package pl.wrona.webserver.gtfs;

import jakarta.annotation.Resource;
import org.igeolab.iot.gtfs.server.api.ShapeApi;
import org.springframework.cloud.openfeign.FeignClient;

@Resource
@FeignClient(value = "warsaw-gtfs-shapes-client", url = "${gtfs.server.warsaw.url}")
public interface WarsawGtfsShapeClient extends ShapeApi {
}
