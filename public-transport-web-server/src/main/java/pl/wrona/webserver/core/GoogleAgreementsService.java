package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.GoogleAgreementsRequest;
import org.igeolab.iot.pt.server.api.model.GoogleAgreementsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.entity.Agency;

import java.util.Optional;

@Service
@AllArgsConstructor
public class GoogleAgreementsService {

    private final GoogleAgreementsRepository googleAgreementsRepository;
    private final AgencyService agencyService;

    public GoogleAgreementsResponse getGoogleAgreements() {
        Agency loggedUser = agencyService.getLoggedAgency();
        return googleAgreementsRepository.findByAgency(loggedUser.getAgencyId())
                .map(entity -> new GoogleAgreementsResponse()
                        .repeatabilityStatement(entity.isRepeatabilityStatement())
                        .accessibilityStatement(entity.isAccessibilityStatement())
                        .ticketSalesStatement(entity.isTicketSalesStatement()))
                .orElse(new GoogleAgreementsResponse());
    }

    @Transactional
    public Status updateGoogleAgreements(GoogleAgreementsRequest googleAgreementsRequest) {
        Agency loggedUser = agencyService.getLoggedAgency();

        Optional<GoogleAgreementsRequest> request = Optional.ofNullable(googleAgreementsRequest);

        GoogleAgreementEntity agreements = googleAgreementsRepository.findByAgency(loggedUser.getAgencyId())
                .map(entity -> {
                    entity.setRepeatabilityStatement(request.map(GoogleAgreementsRequest::getRepeatabilityStatement).orElse(false));
                    entity.setAccessibilityStatement(request.map(GoogleAgreementsRequest::getAccessibilityStatement).orElse(false));
                    entity.setTicketSalesStatement(request.map(GoogleAgreementsRequest::getTicketSalesStatement).orElse(false));
                    return entity;
                })
                .orElse(GoogleAgreementEntity.builder()
                        .agency(loggedUser)
                        .repeatabilityStatement(request.map(GoogleAgreementsRequest::getRepeatabilityStatement).orElse(false))
                        .accessibilityStatement(request.map(GoogleAgreementsRequest::getAccessibilityStatement).orElse(false))
                        .ticketSalesStatement(request.map(GoogleAgreementsRequest::getTicketSalesStatement).orElse(false))
                        .build());

        googleAgreementsRepository.save(agreements);

        return new Status().status(Status.StatusEnum.SUCCESS);
    }
}
