package pl.wrona.webserver.core.brigade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.igeolab.iot.pt.server.api.model.TripMode;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.TripEntity;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brigade_trip")
public class BrigadeTripEntity {

    @Id
    @Column(name = "brigade_trip_id")
    private String brigadeTripId;

    private String line;

    private String name;

    private String variant;

    @Enumerated(value = EnumType.STRING)
    private TripMode mode;

    @Column(name = "trip_sequence")
    private int tripSequence;

    @ManyToOne
    @JoinColumn(name = "root_trip_id", referencedColumnName = "trip_id", nullable = false)
    private TripEntity rootTrip;

    @ManyToOne
    @JoinColumn(name = "brigade_id", nullable = false)
    private BrigadeEntity brigade;

    @Column(name = "departure_time_in_second")
    private int departureTimeInSeconds;

    @Column(name = "variant_designation")
    private String variantDesignation;

    @Column(name = "variant_description")
    private String variantDescription;

    @Column(name = "origin")
    private String origin;

    @Column(name = "destination")
    private String destination;

    @Column(name = "travel_time_in_seconds")
    private int travelTimeInSeconds;

    public String stringifyId(AgencyEntity agencyEntity, BrigadeEntity brigade) {
        return "%s/%s/%s".formatted(agencyEntity.getAgencyCode(), brigade.getBrigadeNumber(), this.getTripSequence());
    }

    public String getRouteId() {
        return "%s/%s".formatted(line, name);
    }

}
