package pl.wrona.webserver.bussiness.trip.measure;

import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.wrona.webserver.BaseIntegrationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TripDistanceMeasureServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TripDistanceMeasureService tripDistanceMeasureService;

    @Test
    void shouldCalculateRealDistance() {
        // given
        Trip trip = new Trip()
                .line("L1")
                .headsign("")
                .communicationVelocity(27)
                .stops(List.of(
                        new StopTime().stopId(142902L).stopName("Chmielnik").lon(20.751886f).lat(50.614586f),
                        new StopTime().stopId(234100L).stopName("Przededworze").lon(20.722895f).lat(50.610844f),
                        new StopTime().stopId(142201L).stopName("Jasień").lon(20.701752f).lat(50.608105f),
                        new StopTime().stopId(143073L).stopName("Holendry").lon(20.682875f).lat(50.6056f),
                        new StopTime().stopId(145581L).stopName("Samostrzałów").lon(20.653494f).lat(50.60081f),
                        new StopTime().stopId(145418L).stopName("Wola Żydowska / DK78 / 01").lon(20.641573f).lat(50.6002f),
                        new StopTime().stopId(145434L).stopName("Żydówek / DK78 / 02").lon(20.630238f).lat(50.60473f),
                        new StopTime().stopId(145643L).stopName("Żydówek / DK78 / 01").lon(20.622282f).lat(50.608337f),
                        new StopTime().stopId(145448L).stopName("Gołuchów 02").lon(20.60794f).lat(50.609314f),
                        new StopTime().stopId(145638L).stopName("Lipnik / DK78 / 01").lon(20.595602f).lat(50.60933f),
                        new StopTime().stopId(145528L).stopName("Lipnik / DK78 / 02").lon(20.583258f).lat(50.606346f),
                        new StopTime().stopId(145543L).stopName("Kije Kościół").lon(20.569538f).lat(50.607536f)
                ));

        // when
        Trip results = tripDistanceMeasureService.measureDistance(trip);

        // then
        assertThat(results).isNotNull();
        assertThat(results.getStops()).hasSize(12);

        assertThat(results.getStops().get(0).getStopName()).isEqualTo("Chmielnik");
        assertThat(results.getStops().get(0).getArrivalTime()).isEqualTo(0);
        assertThat(results.getStops().get(0).getDepartureTime()).isEqualTo(0);
        assertThat(results.getStops().get(0).getMeters()).isEqualTo(0);
        assertThat(results.getStops().get(0).getSeconds()).isEqualTo(0);

        assertThat(results.getStops().get(1).getStopName()).isEqualTo("Przededworze");
        assertThat(results.getStops().get(1).getArrivalTime()).isEqualTo(161);
        assertThat(results.getStops().get(1).getDepartureTime()).isEqualTo(161);
        assertThat(results.getStops().get(1).getSeconds()).isEqualTo(161);
        assertThat(results.getStops().get(1).getMeters()).isEqualTo(2166);

        assertThat(results.getStops().get(2).getStopName()).isEqualTo("Jasień");
        assertThat(results.getStops().get(2).getArrivalTime()).isEqualTo(242);
        assertThat(results.getStops().get(2).getDepartureTime()).isEqualTo(242);
        assertThat(results.getStops().get(2).getSeconds()).isEqualTo(242);
        assertThat(results.getStops().get(2).getMeters()).isEqualTo(3689);

        assertThat(results.getStops().get(3).getStopName()).isEqualTo("Holendry");
        assertThat(results.getStops().get(3).getArrivalTime()).isEqualTo(298);
        assertThat(results.getStops().get(3).getDepartureTime()).isEqualTo(298);
        assertThat(results.getStops().get(3).getSeconds()).isEqualTo(298);
        assertThat(results.getStops().get(3).getMeters()).isEqualTo(5050);
    }

}