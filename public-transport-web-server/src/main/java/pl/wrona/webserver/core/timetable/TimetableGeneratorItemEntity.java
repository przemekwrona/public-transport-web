package pl.wrona.webserver.core.timetable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timetable_generator_item")
//@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class TimetableGeneratorItemEntity {

    @Id
    @Column(name = "timetable_generator_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timetable_generator_item_id_seq")
    @SequenceGenerator(name = "timetable_generator_item_id_seq", sequenceName = "timetable_generator_item_id_seq", allocationSize = 1)
    private Long timetableGeneratorId;

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

//    route_name
//            front_start_time
//    route_line
//            front_payload
//    front_end_time
//            back_interval
//    back_payload
//            back_start_time
//    front_interval
//            agency_id
//    back_end_time
//            calendar_id
}
