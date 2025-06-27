package pl.wrona.webserver.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.wrona.webserver.BaseIntegrationTest;
import pl.wrona.webserver.client.geoapify.Feature;
import pl.wrona.webserver.client.geoapify.GeoResponse;
import pl.wrona.webserver.client.geoapify.GeoapifyService;

import static org.assertj.core.api.Assertions.assertThat;

class GeoapifyServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private GeoapifyService geoapifyService;

    @Test
    void shouldReturnPositionBaseOnAddress() {
        // given
        String text = "Nakielska 3/1; 01-106 Warszawa; Polska";

        // when
        GeoResponse geoResponse = geoapifyService.geoapify(text);

        // then
        assertThat(geoResponse).isNotNull();
        assertThat(geoResponse.features()).hasSize(2);

        Feature firstAddress = geoResponse.features().get(0);
        assertThat(firstAddress.properties().city()).isEqualTo("Warsaw");
        assertThat(firstAddress).isNotNull();
        assertThat(firstAddress.properties().name()).isNull();
        assertThat(firstAddress.properties().street()).isEqualTo("Nakielska");
        assertThat(firstAddress.properties().housenumber()).isEqualTo("3");
        assertThat(firstAddress.properties().suburb()).isEqualTo("Wola");
        assertThat(firstAddress.properties().district()).isEqualTo("Osiedle Sowińskiego");
        assertThat(firstAddress.properties().postcode()).isEqualTo("01-106");
        assertThat(firstAddress.properties().city()).isEqualTo("Warsaw");
        assertThat(firstAddress.properties().rank().confidence()).isEqualTo(0.9);

        Feature secondAddress = geoResponse.features().get(1);
        assertThat(secondAddress).isNotNull();
        assertThat(secondAddress.properties().name()).isEqualTo("Onninen Express");
        assertThat(secondAddress.properties().street()).isEqualTo("Nakielska");
        assertThat(secondAddress.properties().housenumber()).isEqualTo("3");
        assertThat(secondAddress.properties().suburb()).isEqualTo("Wola");
        assertThat(secondAddress.properties().district()).isEqualTo("Osiedle Sowińskiego");
        assertThat(secondAddress.properties().postcode()).isEqualTo("01-106");
        assertThat(secondAddress.properties().city()).isEqualTo("Warsaw");
        assertThat(secondAddress.properties().rank().confidence()).isEqualTo(0.8);
    }

    @Test
    void shouldReturnMostProbableCords() {
        // given
        String text = "Nakielska 3/1; 01-106 Warszawa; Polska";

        // when
        Feature address = geoapifyService.mostProbableAddress(text);

        // then
        assertThat(address).isNotNull();
        assertThat(address.properties().name()).isNull();
        assertThat(address.properties().street()).isEqualTo("Nakielska");
        assertThat(address.properties().housenumber()).isEqualTo("3");
        assertThat(address.properties().suburb()).isEqualTo("Wola");
        assertThat(address.properties().district()).isEqualTo("Osiedle Sowińskiego");
        assertThat(address.properties().postcode()).isEqualTo("01-106");
        assertThat(address.properties().city()).isEqualTo("Warsaw");
        assertThat(address.properties().rank().confidence()).isEqualTo(0.9);
    }

}