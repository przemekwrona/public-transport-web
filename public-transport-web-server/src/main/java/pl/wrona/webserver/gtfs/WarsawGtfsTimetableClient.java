package pl.wrona.webserver.gtfs;

import jakarta.annotation.Resource;
import org.igeolab.iot.gtfs.server.api.StopsApi;
import org.igeolab.iot.gtfs.server.api.TimetableApi;
import org.springframework.cloud.openfeign.FeignClient;

@Resource
@FeignClient(value = "warsaw-gtfs-timetable-client", url = "${gtfs.server.warsaw.url}")
public interface WarsawGtfsTimetableClient extends TimetableApi {
}
