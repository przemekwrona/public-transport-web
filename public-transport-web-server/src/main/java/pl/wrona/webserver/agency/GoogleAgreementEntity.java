package pl.wrona.webserver.agency;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import pl.wrona.webserver.agency.entity.Agency;


@Entity
@Table(name = "google_agreements")
public class GoogleAgreementEntity {

    @Id
    @Column(name = "google_agreement_id")
    private Long googleAgreementId;

    @OneToOne(mappedBy = "agency_id")
    private Agency agency;

    @Column(name = "repeatability_statement")
    private boolean repeatabilityStatement;

    @Column(name = "accessibility_statement")
    private boolean accessibilityStatement;

    @Column(name = "ticket_sales_statement")
    private boolean ticketSalesStatement;
}
