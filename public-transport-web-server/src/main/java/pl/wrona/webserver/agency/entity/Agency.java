package pl.wrona.webserver.agency.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.wrona.webserver.agency.GoogleAgreementEntity;
import pl.wrona.webserver.agency.brigade.BrigadeEntity;
import pl.wrona.webserver.agency.calendar.CalendarEntity;
import pl.wrona.webserver.security.AppUser;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agency {

    @Id
    @Column(name = "agency_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_id_seq")
    @SequenceGenerator(name = "app_user_id_seq", sequenceName = "app_user_id_seq", allocationSize = 1)
    private Long agencyId;

    @Column(name = "agency_code", unique = true)
    private String agencyCode;

    @Column(name = "agency_name")
    private String agencyName;

    @Column(name = "agency_url")
    private String agencyUrl;

    @Column(name = "tax_number")
    private String texNumber;

    @Column(name = "street")
    private String street;

    @Column(name = "house_number")
    private String houseNumber;

    @Column(name = "flat_number")
    private String flatNumber;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "postal_city")
    private String postalCity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "agency_timetable_url")
    private String agencyTimetableUrl;

    @Column(name = "agency_phone")
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

    @ManyToMany
    @JoinTable(name = "app_user_agency",
            joinColumns = @JoinColumn(name = "agency_id"),
            inverseJoinColumns = @JoinColumn(name = "app_user_id"))
    private Set<AppUser> users = new HashSet<>();

    @OneToOne(mappedBy = "agency", cascade = CascadeType.ALL)
    private GoogleAgreementEntity googleAgreement;

}
