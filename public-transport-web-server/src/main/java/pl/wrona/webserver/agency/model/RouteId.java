package pl.wrona.webserver.agency.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class RouteId implements Serializable {

    @Column(name = "route_id")
    private String routeId;

    @ManyToOne
    @JoinColumn(name = "agency_code", referencedColumnName = "agency_code", nullable = false)
    private Agency agency;
}
