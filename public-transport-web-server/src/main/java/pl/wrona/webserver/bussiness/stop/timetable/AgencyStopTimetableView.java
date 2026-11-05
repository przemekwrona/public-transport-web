package pl.wrona.webserver.bussiness.stop.timetable;

import java.util.List;

public record AgencyStopTimetableView(
        String agencyName,
        String stopName,
        List<DirectionRow> directions,
        List<LegendEntry> legend
) {

    public record DirectionRow(
            String direction,
            List<DepartureTime> departures
    ) {
    }

    public record DepartureTime(
            String time,
            List<String> symbols
    ) {
    }

    public record LegendEntry(
            String symbol,
            String description
    ) {
    }
}
