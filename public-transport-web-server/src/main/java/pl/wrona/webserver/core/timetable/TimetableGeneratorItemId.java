package pl.wrona.webserver.core.timetable;

import lombok.Data;

import java.io.Serializable;

@Data
public class TimetableGeneratorItemId implements Serializable {

    private Long calendarId;
    private Long timetableGeneratorId;

}
