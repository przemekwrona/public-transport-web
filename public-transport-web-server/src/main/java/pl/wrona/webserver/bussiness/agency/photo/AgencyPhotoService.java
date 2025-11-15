package pl.wrona.webserver.bussiness.agency.photo;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.io.IOException;

@Service
@AllArgsConstructor
public class AgencyPhotoService {

    private AgencyRepository agencyRepository;

    public Status putAgencyPhoto(String agency, MultipartFile file) {
        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(agency);
        try {
            agencyEntity.setPhoto(file.getBytes());
            agencyRepository.save(agencyEntity);
        } catch (IOException exception) {
        }
        return new Status().status(Status.StatusEnum.SUCCESS);
    }
}
