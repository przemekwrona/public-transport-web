package pl.wrona.webserver.bussiness.route.stop.timetable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RouteStopTimetablePdfRendererTest {

    @Test
    void rendersPolishStopTimetableAsPdf() throws Exception {
        RouteStopTimetablePdfRenderer renderer = new RouteStopTimetablePdfRenderer(templateEngine());
        RouteStopTimetableView timetable = new RouteStopTimetableView(
                "Następna Stacja",
                "L1",
                "Kielce - Kraków",
                "Kielce, Dworzec",
                "Kraków",
                "FRONT",
                List.of(new RouteStopTimetableView.CalendarTimetable(
                        "D",
                        "Dni robocze",
                        "D — Dni robocze",
                        List.of(new RouteStopTimetableView.HourRow(
                                "06",
                                List.of(new RouteStopTimetableView.MinuteCell("05", "S")))))),
                List.of(new RouteStopTimetableView.LegendEntry("S", "Skrócony")));

        byte[] pdf = renderer.render(timetable);

        assertThat(pdf).startsWith("%PDF".getBytes());
        try (PDDocument document = PDDocument.load(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("L1");
            assertThat(text).contains("Kielce, Dworzec");
            assertThat(text).contains("Kierunek:");
            assertThat(text).contains("Kraków");
            assertThat(text).contains("Dni robocze");
            assertThat(text).contains("05");
            assertThat(text).contains("Oznaczenia");
        }
    }

    private static SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
