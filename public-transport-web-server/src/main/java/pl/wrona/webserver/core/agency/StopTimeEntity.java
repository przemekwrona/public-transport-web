package pl.wrona.webserver.core.agency;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wrona.webserver.core.entity.StopEntity;

@Data
@Builder
@Entity
@Table(name = "stop_time")
@NoArgsConstructor
@AllArgsConstructor
public class StopTimeEntity {

    @EmbeddedId
    private StopTimeId stopTimeId;

    @MapsId("tripProfileId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_profile_id")
    private TripProfileEntity tripProfile;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stop_id")
    private StopEntity stopEntity;

    private int calculatedTimeSeconds;
    private int customizedTimeSeconds;
    private int breakSeconds;

    private int distanceMeters;

}
