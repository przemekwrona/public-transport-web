package pl.wrona.webserver.bussiness.agency.photo;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.PutAgencyPhotoRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.io.IOException;

@Service
@AllArgsConstructor
public class AgencyPhotoService {

    private AgencyRepository agencyRepository;

    public Status putAgencyPhoto(String agency, PutAgencyPhotoRequest putAgencyPhotoRequest) throws IOException {
        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(agency);
        agencyEntity.setPhoto(putAgencyPhotoRequest.getFile().getContentAsByteArray());
        agencyRepository.save(agencyEntity);
        return new Status().status(Status.StatusEnum.SUCCESS);
    }
}
