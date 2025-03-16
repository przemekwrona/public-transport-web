package pl.wrona.webserver.agency.brigade;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.BrigadePayload;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.AgencyService;

@Service
@AllArgsConstructor
public class BrigadeService {

    private AgencyService agencyService;

    private final BrigadeRepository brigadeRepository;

    public Status createBrigade(BrigadeBody request) {
        var brigadeEntity = new BrigadeEntity();
        brigadeEntity.setBrigadeNumber(request.getBrigadeNumber());
        brigadeEntity.setAgency(agencyService.getLoggedAgency());

        var savedBrigade = brigadeRepository.save(brigadeEntity);

        return new Status().status(Status.StatusEnum.CREATED);
    }

    public BrigadeBody getBrigadeByBrigadeName(BrigadePayload brigadePayload) {
        return brigadeRepository.findByBrigadeNumber(brigadePayload.getBrigadeNumber())
                .map(brigadeEntity -> new BrigadeBody()
                        .brigadeNumber(brigadeEntity.getBrigadeNumber()))
                .orElse(null);
    }
}
