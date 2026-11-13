package pl.wrona.webserver.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Builder
@Table(name = "subdivision")
@NoArgsConstructor
@AllArgsConstructor
public class SubdivisionEntity {

    @Id
    @Column(name = "teryt", length = 7)
    private String teryt;

    @ToString.Exclude
    @Column(name = "geometry", columnDefinition = "text")
    private String geometry;

}
