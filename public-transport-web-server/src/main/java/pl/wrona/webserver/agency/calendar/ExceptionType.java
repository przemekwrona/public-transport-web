package pl.wrona.webserver.agency.calendar;

public enum ExceptionType {
    ADDED(1),
    REMOVED(2);

    private int gtfsCode;

    ExceptionType(int gtfsCode) {
        this.gtfsCode = gtfsCode;
    }
}
