package pl.wrona.webserver.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@Table(name = "teryt")
@NoArgsConstructor
@AllArgsConstructor
public class TerytEntity {

    @Id
    @Column(name = "TERYT", length = 4)
    private String teryt;

    @Column(name = "IDTERC", length = 7)
    private String idterc;

    @Column(name = "WOJ", length = 2)
    private String woj;

    @Column(name = "POW", length = 2)
    private String pow;

    @Column(name = "GMI", length = 2)
    private String gmi;

    @Column(name = "RODZ", length = 1)
    private String rodz;

    @Column(name = "NAZWA", length = 50)
    private String nazwa;

    @Column(name = "NAZWA_DOD", length = 50)
    private String nazwaDod;

}
