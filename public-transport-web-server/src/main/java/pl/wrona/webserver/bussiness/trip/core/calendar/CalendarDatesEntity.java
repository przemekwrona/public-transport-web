package pl.wrona.webserver.bussiness.trip.core.calendar;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "calendar_dates")
public class CalendarDatesEntity {

    @EmbeddedId
    private CalendarDatesId calendarDatesId;

    @Enumerated(EnumType.STRING)
    private ExceptionType exceptionType;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false, insertable = false, updatable = false)
    private CalendarEntity calendar;

    public ServiceDate getServiceDate() {
        return new ServiceDate(calendarDatesId.getDate().getYear(), calendarDatesId.getDate().getMonthValue(), calendarDatesId.getDate().getDayOfMonth());
    }
}
