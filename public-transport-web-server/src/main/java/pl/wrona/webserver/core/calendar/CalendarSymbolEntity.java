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
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import pl.wrona.webserver.core.brigade.BrigadeEntity;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemEntity;

import java.time.LocalDate;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "calendar_symbol")
public class CalendarSymbolEntity {

    @Id
    @Column(name = "calendar_symbol_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_symbol_id_seq")
    @SequenceGenerator(name = "calendar_symbol_id_seq", sequenceName = "calendar_symbol_id_seq", allocationSize = 1)
    private Long serviceId;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private AgencyEntity agency;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL)
    private Set<BrigadeEntity> brigadeEntities;

    private String designation;
    private String description;

    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL)
    private Set<CalendarDatesEntity> calendarDates;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL)
    private Set<TimetableGeneratorItemEntity> timetableGenerators;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "calendar_item_id", nullable = false)
    private CalendarItemEntity calendarItem;

}
