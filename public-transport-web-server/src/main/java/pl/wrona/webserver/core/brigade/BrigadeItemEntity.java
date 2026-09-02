package pl.wrona.webserver.core.brigade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brigade_item")
public class BrigadeItemEntity {

    @Id
    @Column(name = "brigade_item_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brigade_item_id_seq")
    @SequenceGenerator(name = "brigade_item_id_seq", sequenceName = "brigade_item_id_seq", allocationSize = 1)
    private Long brigadeItemId;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private AgencyEntity agency;

    @ManyToOne
    @JoinColumn(name = "calendar_item_id", nullable = false)
    private CalendarItemEntity calendarItem;

    @ManyToOne
    @JoinColumn(name = "default_route")
    private RouteEntity defaultRoute;

    @Column(nullable = false, length = 50)
    private String name;

    private Integer sequence;

    private String sequenceHex;

}
