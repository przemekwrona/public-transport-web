package pl.wrona.webserver.bussiness.pdf.trip;

import java.util.List;

public record PdfStopDeparture(long stopId, String stopName, List<PdfDeparture> departures) {

    public void add(PdfDeparture departure) {
        this.departures.add(departure);
    }
}
