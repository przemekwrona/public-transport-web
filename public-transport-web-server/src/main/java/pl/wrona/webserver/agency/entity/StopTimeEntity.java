package pl.wrona.webserver.agency.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@Entity
@Table(name="stop_time")
@NoArgsConstructor
@AllArgsConstructor
public class StopTimeEntity {

    @EmbeddedId
    private StopTimeId stopTimeId;

    @ManyToOne
    @JoinColumn(name = "stop_id")
    private Stop stop;

    @ManyToOne
    @JoinColumn(name = "trip_id", referencedColumnName = "trip_id", insertable = false, updatable = false, nullable = false)
    private TripEntity trip;

    private int arrivalSecond;
    private int departureSecond;
    private int distanceMeters;
}
