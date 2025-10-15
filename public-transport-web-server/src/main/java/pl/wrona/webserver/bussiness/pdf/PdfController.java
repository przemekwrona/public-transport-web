package pl.wrona.webserver.bussiness.pdf;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.pdf.api.PdfApi;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.pdf.trip.PdfTripService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class PdfController implements PdfApi {

    private final PdfTripService pdfTripService;

    @Override
    public ResponseEntity<Resource> downloadTripPdf(String line, String name) {
        return ResponseEntity.ok(pdfTripService.downloadTripPdf(line, name));
    }
}
