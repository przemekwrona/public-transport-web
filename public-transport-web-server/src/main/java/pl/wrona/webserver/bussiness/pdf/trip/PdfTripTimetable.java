package pl.wrona.webserver.bussiness.pdf.trip;

import java.util.LinkedHashMap;
import java.util.List;

public record PdfTripTimetable(String line, String name, String origin, String destination, List<PdfStopDeparture> stops, LinkedHashMap<Long, PdfStopDeparture> stopsDictionary) {

    public void putStopIfAbsent(PdfStopDeparture stop) {
        stopsDictionary.computeIfAbsent(stop.stopId(), id -> {
            stops.add(stop);
            return stop;
        });
    }

    public void putDeparture(PdfDeparture departure) {
        this.stopsDictionary.get(departure.stopId()).add(departure);
    }
}
