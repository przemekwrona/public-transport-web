package pl.wrona.webserver.agency.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wrona.webserver.agency.brigade.BrigadeEntity;
import pl.wrona.webserver.agency.calendar.CalendarEntity;
import pl.wrona.webserver.security.AppUser;

import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Agency {

    @Id
    private Long agencyId;

    @Column(name="agency_code", unique = true)
    private String agencyCode;

    @Column(name="agency_name")
    private String agencyName;

    @Column(name="agency_url")
    private String agencyUrl;

    @Column(name="agency_timetable_url")
    private String agencyTimetableUrl;

    @Column(name="agency_phone")
    private String agencyPhone;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "agency_owner_id", referencedColumnName = "app_user_id")
    private AppUser appUser;

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
    private Set<RouteEntity> routeEntities;

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
    private Set<BrigadeEntity> brigades;

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
    private Set<CalendarEntity> calendars;
}
