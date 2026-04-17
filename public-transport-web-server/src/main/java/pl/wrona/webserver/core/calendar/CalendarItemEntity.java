package pl.wrona.webserver.core.calendar;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.time.LocalDate;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "calendar_item")
public class CalendarItemEntity {

    @Id
    @Column(name = "calendar_item_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_item_id_seq")
    @SequenceGenerator(name = "calendar_item_id_seq", sequenceName = "calendar_item_id_seq", allocationSize = 1)
    private Long calendarItemId;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private AgencyEntity agency;

    @Column(name = "calendar_name")
    private String calendarName;

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "calendarItem", cascade = CascadeType.ALL)
    private Set<CalendarSymbolEntity> calendarSymbols;

}
