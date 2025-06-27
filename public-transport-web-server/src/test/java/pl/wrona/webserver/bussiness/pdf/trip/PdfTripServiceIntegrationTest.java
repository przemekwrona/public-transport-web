package pl.wrona.webserver.bussiness.pdf.trip;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.wrona.webserver.BaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class PdfTripServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PdfTripService pdfTripService;

    @Test
    void shouldPrepareDataForGenerationFormWithDeparturesForDepartamentOfCommunication() {
        // given
        var line = "L1";
        var name = "Chmielnik - Kije";

        // when
        var routeVariables = pdfTripService.routeVariables(line, name);

        // then
        assertThat("").isEmpty();
    }

}