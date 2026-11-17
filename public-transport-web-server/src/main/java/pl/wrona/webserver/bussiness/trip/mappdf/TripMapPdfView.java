package pl.wrona.webserver.bussiness.trip.mappdf;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record TripMapPdfView(
        String agencyCode,
        String line,
        String routeName,
        String tripCode,
        String variantDesignation,
        String headsign,
        String mapHtml,
        List<Municipality> municipalities
) {

    public String municipalitiesSummary() {
        if (municipalities == null || municipalities.isEmpty()) {
            return "";
        }
        return municipalities.stream()
                .map(item -> item.gmina() + " (" + item.kilometers() + " km, " + item.percent() + "%)")
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .collect(Collectors.joining(", "));
    }

    public record Municipality(
            String teryt,
            String wojewodztwo,
            String powiat,
            String gmina,
            String kilometers,
            String percent
    ) {
    }
}
