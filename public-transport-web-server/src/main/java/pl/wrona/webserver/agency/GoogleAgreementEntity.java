package pl.wrona.webserver.agency;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.wrona.webserver.agency.entity.Agency;

@Entity
@Table(name = "google_agreements")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAgreementEntity {

    @Id
    @Column(name = "google_agreement_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "google_agreements_id_seq")
    @SequenceGenerator(name = "google_agreements_id_seq", sequenceName = "google_agreements_id_seq", allocationSize = 1)
    private Long googleAgreementId;

    @OneToOne
    @JoinColumn(name = "agency_id", referencedColumnName = "agency_id")
    private Agency agency;

    @Column(name = "repeatability_statement")
    private boolean repeatabilityStatement;

    @Column(name = "accessibility_statement")
    private boolean accessibilityStatement;

    @Column(name = "ticket_sales_statement")
    private boolean ticketSalesStatement;
}
