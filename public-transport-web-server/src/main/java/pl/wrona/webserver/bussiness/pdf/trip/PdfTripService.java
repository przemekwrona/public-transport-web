package pl.wrona.webserver.bussiness.pdf.trip;

//import com.itextpdf.io.font.FontProgramFactory;
//import com.itextpdf.kernel.font.PdfFontFactory;
//import com.itextpdf.kernel.geom.PageSize;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.element.AreaBreak;
//import com.itextpdf.layout.element.Cell;
//import com.itextpdf.layout.element.Paragraph;
//import com.itextpdf.layout.element.Table;
//import com.itextpdf.layout.properties.AreaBreakType;
//import com.itextpdf.layout.properties.TextAlignment;
//import com.itextpdf.styledxmlparser.jsoup.Jsoup;
//import com.itextpdf.styledxmlparser.jsoup.nodes.Document;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.StopTimeService;
import pl.wrona.webserver.core.brigade.BrigadeTripEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;

@Slf4j
@Service
@AllArgsConstructor
public class PdfTripService {

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("H:mm");

    private final PdfTripTripRepository pdfTripTripRepository;
    private final PdfTripStopTimeRepository pdfTripStopTimeRepository;
    private final PdfTripBrigadeTripRepository pdfTripBrigadeTripRepository;
    private final StopTimeService stopTimeService;
    private final TemplateEngine templateEngine;
    private final ResourceLoader resourceLoader;
    private final AgencyService agencyService;

    public Map<String, Object> routeVariables(String line, String name) {
        Map<String, Object> variables = new HashMap<>();

        var timetables = pdfTripTripRepository.findAllByRoute(line, name).stream()
                .map(mode -> processTimetable(mode.getLine(), mode.getName(), mode.getMode()))
                .toList();

        var agency = this.agencyService.getLoggedAgency();

        variables.put("timetables", timetables);
        variables.put("now", LocalDateTime.now());
        variables.put("companyName", agency.getAgencyName());

        return variables;
    }

    private PdfTripTimetable processTimetable(String line, String name, TripVariantMode mode) {

        List<TripEntity> trips = pdfTripTripRepository.findAllByRoute(line, name, mode);

        TripEntity mainTrip = trips.stream().filter(TripEntity::isMainVariant).findFirst().orElse(new TripEntity());

        PdfTripTimetable timetable = new PdfTripTimetable(line, name, mainTrip.getOriginStopName(), mainTrip.getDestinationStopName(), new ArrayList<>(), new LinkedHashMap<>());

        for (TripEntity trip : trips) {
            for (BrigadeTripEntity brigadeTrip : pdfTripBrigadeTripRepository.findByRootTrip(trip)) {
                stopTimeService.getAllStopTimesByTrip(brigadeTrip.getRootTrip())
                        .forEach(stopTime -> {
                            PdfStopDeparture pdfDeparture = new PdfStopDeparture(stopTime.getStopEntity().getStopId(), stopTime.getStopEntity().getName(), new ArrayList<>());
                            timetable.putStopIfAbsent(pdfDeparture);
                        });

                stopTimeService.getAllStopTimesByTrip(brigadeTrip.getRootTrip()).stream()
                        .map((StopTimeEntity stopTime) -> new PdfDeparture(stopTime.getStopEntity().getStopId(),
                                stopTime.getStopEntity().getName(),
                                stopTime.getStopTimeId().getStopSequence(),
                                brigadeTrip.getBrigade().getCalendar().getDesignation(),
                                LocalTime.ofSecondOfDay(brigadeTrip.getDepartureTimeInSeconds()).plusSeconds(stopTime.getCalculatedTimeSeconds()).format(HHMM)))
                        .forEach(timetable::putDeparture);
            }
        }

        return timetable;
    }

    public Resource downloadTripPdf(String line, String name) {
        Context context = new Context();
        context.setVariables(routeVariables(line, name));
        String html = templateEngine.process("line/route-timetable.html", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(resourceLoader.getResource("classpath:/templates/Roboto-Light.ttf").getFile(), "Roboto");

            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return new ByteArrayResource(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
