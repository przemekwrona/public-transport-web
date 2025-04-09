package pl.wrona.webserver.agency.calendar;

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
import pl.wrona.webserver.agency.brigade.BrigadeEntity;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.agency.entity.RouteEntity;

import java.time.LocalDate;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "calendar")
public class CalendarEntity {

    @Id
    @Column(name = "service_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_id_seq")
    @SequenceGenerator(name = "service_id_seq", sequenceName = "service_id_seq",  allocationSize=1)
    private Long serviceId;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL)
    private Set<BrigadeEntity> brigadeEntities;

    @Column(name = "calendar_name")
    private String calendarName;

    private String designation;
    private String description;

    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL)
    private Set<CalendarDatesEntity> calendarDates;
}
