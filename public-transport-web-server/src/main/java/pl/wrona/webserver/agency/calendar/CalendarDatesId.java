package pl.wrona.webserver.agency.calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDatesId implements Serializable {

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "date")
    private LocalDate date;

}
