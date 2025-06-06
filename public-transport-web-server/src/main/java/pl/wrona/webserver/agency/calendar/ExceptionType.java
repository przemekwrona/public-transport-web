package pl.wrona.webserver.agency.calendar;

import lombok.Getter;

public enum ExceptionType {
    ADDED(1),
    REMOVED(2);

    @Getter
    private int gtfsCode;

    ExceptionType(int gtfsCode) {
        this.gtfsCode = gtfsCode;
    }
}
