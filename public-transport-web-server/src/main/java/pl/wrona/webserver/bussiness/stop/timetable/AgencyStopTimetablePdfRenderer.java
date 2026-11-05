package pl.wrona.webserver.bussiness.stop.timetable;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.wrona.webserver.exception.BusinessException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Locale;

@Component
@AllArgsConstructor
public class AgencyStopTimetablePdfRenderer {

    private static final String TEMPLATE = "stop/agency-stop-timetable";
    private static final String FONT_FAMILY = "Liberation Sans";
    private static final String FONT_REGULAR = "/fonts/LiberationSans-Regular.ttf";
    private static final String FONT_BOLD = "/fonts/LiberationSans-Bold.ttf";

    private final SpringTemplateEngine templateEngine;

    public byte[] render(AgencyStopTimetableView timetable) {
        Context context = new Context(new Locale("pl", "PL"));
        context.setVariable("timetable", timetable);
        context.setVariable("now", LocalDateTime.now());
        String html = templateEngine.process(TEMPLATE, context);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);
            builder.useFont(() -> fontStream(FONT_REGULAR), FONT_FAMILY);
            builder.useFont(() -> fontStream(FONT_BOLD), FONT_FAMILY, 700, PdfRendererBuilder.FontStyle.NORMAL, true);
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException("ERROR:202611051137", "Could not generate agency stop timetable PDF");
        }
    }

    private static InputStream fontStream(String classpathLocation) {
        InputStream font = AgencyStopTimetablePdfRenderer.class.getResourceAsStream(classpathLocation);
        if (font == null) {
            throw new BusinessException("ERROR:202611051138", "PDF font " + classpathLocation + " is missing");
        }
        return font;
    }
}
