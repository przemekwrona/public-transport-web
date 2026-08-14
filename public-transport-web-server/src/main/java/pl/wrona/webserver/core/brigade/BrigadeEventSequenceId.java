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
public class BrigadeEventSequenceId implements Serializable {

    @Column(name = "calendar_code", length = 4, nullable = false)
    private String calendarCode;

    @Column(name = "calendar_symbol", length = 4, nullable = false)
    private String calendarSymbol;

}
