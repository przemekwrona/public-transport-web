package pl.wrona.webserver.bussiness.pdf.trip;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.AreaBreakType;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeTrip;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.StopTimeService;
import pl.wrona.webserver.agency.brigade.BrigadeTripEntity;
import pl.wrona.webserver.agency.entity.StopTimeEntity;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.itextpdf.kernel.pdf.PdfName.BaseFont;

@Service
@AllArgsConstructor
public class PdfTripService {

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("H:mm");

    private final PdfTripTripRepository pdfTripTripRepository;
    private final PdfTripStopTimeRepository pdfTripStopTimeRepository;
    private final PdfTripBrigadeTripRepository pdfTripBrigadeTripRepository;
    private final StopTimeService stopTimeService;

    public Resource downloadTripPdf(String line, String name) {
        // Create output stream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Initialize PDF writer with byte stream
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.setFontSize(8);

        pdfTripTripRepository.findAllByRoute(line, name).forEach(trip -> {
            var brigadeTrips = pdfTripBrigadeTripRepository.findByRootTrip(trip);
            var firstBrigadeTrip = brigadeTrips.get(0);

            var stopTimes = stopTimeService.getAllStopTimesByTrip(firstBrigadeTrip.getRootTrip());
            var stopTimeByStop = stopTimes.stream().collect(Collectors.groupingBy(StopTimeEntity::getStopEntity));

            List<Float> columnWidths = brigadeTrips.stream()
                    .map(i -> 30F)
                    .collect(Collectors.toCollection(ArrayList::new));

            // Adjust the last column to fill remaining space
            columnWidths.add(600F - columnWidths.size() * 30F);

            // Sort in descending order
            List<Float> sorted = columnWidths.stream()
                    .sorted(Collections.reverseOrder())
                    .toList();

            // Convert to float[]
            float[] columnWidthsArray = new float[sorted.size()];
            for (int i = 0; i < sorted.size(); i++) {
                columnWidthsArray[i] = sorted.get(i);
            }

            Table table = new Table(columnWidthsArray);

            // Add table headers
            table.addHeaderCell(new Cell().add(new Paragraph("Przystanek")));

            brigadeTrips.forEach(b -> {
                table.addHeaderCell(new Cell().add(new Paragraph("D").setTextAlignment(TextAlignment.CENTER)));
            });

            stopTimes.forEach(stopTime -> {
                // Add rows
                table.addCell(new Cell().add(new Paragraph(stopTime.getStopEntity().getName()).setTextAlignment(TextAlignment.LEFT)));

                for (BrigadeTripEntity brigadeTrip : brigadeTrips) {
                    table.addCell(stopTime.getDepartureTime(brigadeTrip.getDepartureTimeInSeconds()).format(HHMM))
                            .setTextAlignment(TextAlignment.CENTER);
                }
            });

            document.add(new Paragraph("%s: %s-%s".formatted("", trip.getOriginStopName(), trip.getDestinationStopName())));

            // Add table to document
            document.add(table);

            document.add(new Paragraph("%s: %s".formatted("D", "kursuje od poniedziałku do piątku")));

            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        });


        // Close document (important!)
        document.close();

        // Get PDF as byte array
        byte[] pdfBytes = byteArrayOutputStream.toByteArray();

        // Optional: Do something with the PDF bytes
        System.out.println("PDF generated. Size in bytes: " + pdfBytes.length);

//        PdfTripBuilder pdfTripBuilder = new PdfTripBuilder();

        return new ByteArrayResource(pdfBytes);
    }


}
