package pl.wrona.webserver.bussiness.route.stop.timetable;

import java.util.List;

public record RouteStopTimetableView(
        String agencyName,
        String line,
        String routeName,
        String stopName,
        String direction,
        String tripMode,
        List<CalendarTimetable> calendars,
        List<LegendEntry> legend
) {

    public record CalendarTimetable(
            String designation,
            String description,
            String title,
            List<HourRow> hours
    ) {
    }

    public record HourRow(
            String hour,
            List<MinuteCell> minutes
    ) {
    }

    public record MinuteCell(
            String minute,
            String symbol
    ) {
    }

    public record LegendEntry(
            String symbol,
            String description
    ) {
    }
}
