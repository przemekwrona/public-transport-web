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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brigade_group", uniqueConstraints = {
        @UniqueConstraint(name = "uq_brigade_group_item_calendar_symbol",
                columnNames = {"brigade_item_id", "calendar_symbol_id"})
})
public class BrigadeGroupEntity {

    @Id
    @Column(name = "brigade_group_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brigade_group_id_seq")
    @SequenceGenerator(name = "brigade_group_id_seq", sequenceName = "brigade_group_id_seq", allocationSize = 1)
    private Long brigadeGroupId;

    @ManyToOne
    @JoinColumn(name = "brigade_item_id", nullable = false)
    private BrigadeItemEntity brigadeItem;

    @ManyToOne
    @JoinColumn(name = "calendar_symbol_id", nullable = false)
    private CalendarSymbolEntity calendarSymbol;

}
