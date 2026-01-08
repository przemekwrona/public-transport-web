package pl.wrona.webserver.core.timetable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "timetable_generator")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableGeneratorEntity {

    @Id
    @Column(name = "timetable_generator_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timetable_generator_id_seq")
    @SequenceGenerator(name = "timetable_generator_id_seq", sequenceName = "timetable_generator_id_seq", allocationSize = 1)
    private Long timetableGeneratorId;

    @OneToMany(mappedBy = "timetableGenerator", cascade = CascadeType.ALL)
    private Set<TimetableGeneratorItemEntity> timetableGenerators;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private AgencyEntity agency;

}
