package pl.wrona.webserver.bussiness.agency.photo;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.PutAgencyPhotoRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AgencyPhotoService {

    public Status putAgencyPhoto(String agency, PutAgencyPhotoRequest putAgencyPhotoRequest) {
        return null;
    }
}
