package pl.wrona.webserver.bussiness.agency.address;

import org.igeolab.iot.pt.server.api.model.AgencyAddress;
import org.igeolab.iot.pt.server.api.model.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.client.geoapify.GeoapifyService;
import pl.wrona.webserver.client.geoapify.geocode.Feature;
import pl.wrona.webserver.client.geoapify.geocode.GeoResponse;
import pl.wrona.webserver.client.geoapify.geocode.Geometry;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyAddressUpdaterServiceTest {

    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private GeoapifyService geoapifyService;

    @InjectMocks
    private AgencyAddressUpdaterService agencyAddressUpdaterService;

    @Test
    void storesTheBusinessAddressAndTheFirstGeocodedPoint() {
        AgencyEntity agency = new AgencyEntity();
        when(agencyRepository.findByAgencyCodeEquals("NEXTSTOP")).thenReturn(agency);
        when(geoapifyService.geoapify("Rynek 5/2; 26-020 Chmielnik")).thenReturn(point(21.0, 50.6));

        Status status = agencyAddressUpdaterService.updateAgencyAddress("NEXTSTOP", address("2"));

        assertThat(status.getStatus()).isEqualTo(Status.StatusEnum.SUCCESS);
        assertThat(agency.getStreet()).isEqualTo("Rynek");
        assertThat(agency.getHouseNumber()).isEqualTo("5");
        assertThat(agency.getFlatNumber()).isEqualTo("2");
        assertThat(agency.getPostalCode()).isEqualTo("26-020");
        assertThat(agency.getPostalCity()).isEqualTo("Chmielnik");
        assertThat(agency.getLongitude()).isEqualTo(21.0);
        assertThat(agency.getLatitude()).isEqualTo(50.6);
        verify(agencyRepository).save(agency);
    }

    @Test
    void formatsAnAddressWithoutAFlatNumberAndKeepsCoordinatesWhenGeocodingFindsNothing() {
        AgencyEntity agency = new AgencyEntity();
        agency.setLongitude(1.0);
        agency.setLatitude(2.0);
        when(agencyRepository.findByAgencyCodeEquals("NEXTSTOP")).thenReturn(agency);
        when(geoapifyService.geoapify("Rynek 5; 26-020 Chmielnik")).thenReturn(new GeoResponse("FeatureCollection", List.of(), null));

        agencyAddressUpdaterService.updateAgencyAddress("NEXTSTOP", address(""));

        assertThat(agency.getFlatNumber()).isEmpty();
        assertThat(agency.getLongitude()).isEqualTo(1.0);
        assertThat(agency.getLatitude()).isEqualTo(2.0);
    }

    private static AgencyAddress address(String flatNumber) {
        return new AgencyAddress()
                .street("Rynek")
                .houseNumber("5")
                .flatNumber(flatNumber)
                .postalCode("26-020")
                .postalCity("Chmielnik");
    }

    private static GeoResponse point(double longitude, double latitude) {
        Feature feature = new Feature("Feature", new Geometry("Point", List.of(longitude, latitude)), null, null);
        return new GeoResponse("FeatureCollection", List.of(feature), null);
    }
}
