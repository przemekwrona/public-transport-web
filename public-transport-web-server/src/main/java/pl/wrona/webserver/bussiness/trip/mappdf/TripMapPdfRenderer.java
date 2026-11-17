package pl.wrona.webserver.bussiness.trip.mappdf;

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
public class TripMapPdfRenderer {

    private static final String TEMPLATE = "trip/trip-map";
    private static final String FONT_FAMILY = "Liberation Sans";
    private static final String FONT_REGULAR = "/fonts/LiberationSans-Regular.ttf";
    private static final String FONT_BOLD = "/fonts/LiberationSans-Bold.ttf";

    private final SpringTemplateEngine templateEngine;

    public byte[] render(TripMapPdfView view) {
        Context context = new Context(new Locale("pl", "PL"));
        context.setVariable("view", view);
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
            throw new BusinessException("ERROR:202611140010", "Could not generate trip map PDF");
        }
    }

    private static InputStream fontStream(String classpathLocation) {
        InputStream font = TripMapPdfRenderer.class.getResourceAsStream(classpathLocation);
        if (font == null) {
            throw new BusinessException("ERROR:202611140011", "PDF font " + classpathLocation + " is missing");
        }
        return font;
    }
}
