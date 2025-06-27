package pl.wrona.webserver.bussiness.pdf.trip;

public record PdfDeparture(
        Long stopId,
        String stopName,
        int stopSequence,
        String designation,
        String departureTime) {
}
