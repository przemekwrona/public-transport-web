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

//    @ManyToOne
//    @JoinColumn(name = "trip_id", referencedColumnName = "trip_id")
//    private Trip trip;
//    private int order;

    @EmbeddedId
    private StopTimeId stopTimeId;

    @ManyToOne
    @JoinColumn(name = "stop_id")
    private Stop stop;

    private LocalTime arrivalTime;
    private LocalTime departureTime;
}
