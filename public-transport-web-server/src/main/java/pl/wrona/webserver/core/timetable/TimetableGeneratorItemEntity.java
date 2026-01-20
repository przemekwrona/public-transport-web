package pl.wrona.webserver.core.timetable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.wrona.webserver.core.calendar.CalendarEntity;

import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timetable_generator_item")
@IdClass(TimetableGeneratorItemEntity.class)
public class TimetableGeneratorItemEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private CalendarEntity calendar;

    @Id
    @ManyToOne
    @JoinColumn(name = "timetable_generator_id", nullable = false)
    private TimetableGeneratorEntity timetableGenerator;

    @Column(name = "front_start_time")
    private LocalTime frontStartTime;

    @Column(name = "front_end_time")
    private LocalTime frontEndTime;

    @Column(name = "front_interval")
    private int frontInterval;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "front_payload", columnDefinition = "jsonb")
    private List<TimetableDeparture> frontPayload;

    @Column(name = "back_start_time")
    private LocalTime backStartTime;

    @Column(name = "back_end_time")
    private LocalTime backEndTime;

    @Column(name = "back_interval")
    private int backInterval;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "back_payload", columnDefinition = "jsonb")
    private List<TimetableDeparture> backPayload;

}
