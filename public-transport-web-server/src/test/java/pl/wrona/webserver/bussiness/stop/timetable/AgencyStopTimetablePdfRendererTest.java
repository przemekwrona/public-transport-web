package pl.wrona.webserver.bussiness.stop.timetable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgencyStopTimetablePdfRendererTest {

    @Test
    void rendersWholeStopTimetableAsPdf() throws Exception {
        AgencyStopTimetablePdfRenderer renderer = new AgencyStopTimetablePdfRenderer(templateEngine());
        AgencyStopTimetableView timetable = new AgencyStopTimetableView(
                "Następna Stacja",
                "Kielce, Dworzec",
                List.of(new AgencyStopTimetableView.DirectionRow(
                        "Dworzec / Kraków",
                        List.of(new AgencyStopTimetableView.DepartureTime("06:05", List.of("D", "S"))))),
                List.of(
                        new AgencyStopTimetableView.LegendEntry("D", "Dni robocze"),
                        new AgencyStopTimetableView.LegendEntry("S", "Skrócony")));

        byte[] pdf = renderer.render(timetable);

        assertThat(pdf).startsWith("%PDF".getBytes());
        try (PDDocument document = PDDocument.load(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Kielce, Dworzec");
            assertThat(text).contains("Kierunek / miejscowość docelowa");
            assertThat(text).contains("Dworzec / Kraków");
            assertThat(text).contains("06:05");
            assertThat(text).contains("Oznaczenia");
            assertThat(text).contains("Dni robocze");
            assertThat(text).contains("Skrócony");
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
