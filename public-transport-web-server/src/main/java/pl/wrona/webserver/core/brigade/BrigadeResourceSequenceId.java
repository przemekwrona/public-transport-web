package pl.wrona.webserver.core.brigade;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class BrigadeResourceSequenceId implements Serializable {

    @Column(name = "agency_code", length = 15, nullable = false)
    private String agencyCode;

    @Column(name = "calendar_item_sequence", nullable = false)
    private Integer calendarItemSequence;

    @Column(name = "calendar_symbol", length = 4, nullable = false)
    private String calendarSymbol;

}
