package pl.wrona.webserver.bussiness.stop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.entity.StopEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StopQueryServiceTest {

    @Mock
    private StopTimeRepository stopTimeRepository;

    @InjectMocks
    private StopQueryService stopQueryService;

    @Test
    void findFirstAndLastStopsReturnsMapKeyedByProfile() {
        var firstStop = stop(10L);
        var lastStop = stop(20L);
        var profile = profile(5L);

        when(stopTimeRepository.findFirstAndLastByTripProfileIds(Set.of(5L)))
                .thenReturn(List.of(stopTime(profile, 8, lastStop), stopTime(profile, 1, firstStop)));

        Map<TripProfileEntity, FirstAndLastStop> result = stopQueryService.findFirstAndLastStops(List.of(profile));

        assertEquals(1, result.size());
        assertEquals(firstStop, result.get(profile).firstStop());
        assertEquals(lastStop, result.get(profile).lastStop());
    }

    @Test
    void findFirstAndLastStopsReturnsEmptyMapWhenNoProfiles() {
        assertTrue(stopQueryService.findFirstAndLastStops(List.of()).isEmpty());
    }

    private static StopTimeEntity stopTime(TripProfileEntity profile, int sequence, StopEntity stop) {
        var stopTime = new StopTimeEntity();
        stopTime.setStopTimeId(new StopTimeId(profile.getTripProfileId(), sequence));
        stopTime.setTripProfile(profile);
        stopTime.setStopEntity(stop);
        return stopTime;
    }

    private static TripProfileEntity profile(Long id) {
        var profile = new TripProfileEntity();
        profile.setTripProfileId(id);
        return profile;
    }

    private static StopEntity stop(Long id) {
        var stop = new StopEntity();
        stop.setStopId(id);
        return stop;
    }
}
