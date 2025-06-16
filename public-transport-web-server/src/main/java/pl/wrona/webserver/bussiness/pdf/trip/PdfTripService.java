package pl.wrona.webserver.bussiness.pdf.trip;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PdfTripService {

    private final PdfTripTripRepository pdfTripTripRepository;
    private final PdfTripStopTimeRepository pdfTripStopTimeRepository;

    public Resource downloadTripPdf(String line, String name) {
        PdfTripBuilder pdfTripBuilder = new PdfTripBuilder();

        pdfTripTripRepository.findAllByRoute(line, name)
                .forEach(trip -> {
                    var stopTimes = pdfTripStopTimeRepository.findAllByTrip(trip);

                    pdfTripBuilder.append(trip, stopTimes);
                });

        return null;
    }


}
