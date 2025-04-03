package pl.wrona.webserver.agency.calendar;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "calendar_dates")
public class CalendarDatesEntity {

    @EmbeddedId
    private CalendarDatesId calendarDatesId;

    private String exceptionType;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false, insertable = false, updatable = false)
    private CalendarEntity calendar;
}
