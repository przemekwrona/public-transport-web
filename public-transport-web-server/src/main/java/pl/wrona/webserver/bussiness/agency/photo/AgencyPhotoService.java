package pl.wrona.webserver.bussiness.agency.photo;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.wrona.webserver.core.AgencyPhotoRepository;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.AgencyPhotoEntity;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AgencyPhotoService {

    public static final int _2MB = 2 * 1024 * 1024;
    private AgencyRepository agencyRepository;
    private AgencyPhotoRepository agencyPhotoRepository;

    @Transactional
    @PreAgencyAuthorize
    public Status putAgencyPhoto(String instance, MultipartFile file) {
        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(instance);

        if (file.getSize() > _2MB) {
            throw new BusinessException("ERROR:202511191254", "File exceed 2MB");
        }
        try {
            AgencyPhotoEntity agencyPhoto = new AgencyPhotoEntity();
            agencyPhoto.setPhoto(file.getBytes());
            agencyPhoto.setContentType(file.getContentType());
            agencyPhoto.setContentSizeKb(file.getSize() / 1024);
            agencyPhoto.setCreatedAt(LocalDateTime.now());
            agencyPhoto.setAgency(agencyEntity);

            agencyPhotoRepository.deleteAllByAgency(agencyEntity);
            agencyPhotoRepository.save(agencyPhoto);
        } catch (IOException exception) {
            throw new BusinessException("ERROR:202511150747", exception.getMessage());
        }
        return new Status().status(Status.StatusEnum.SUCCESS);
    }

    @PreAgencyAuthorize
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getAgencyPhoto(String instance) throws IOException {
        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(instance);
        var photo = agencyPhotoRepository.findFirstByAgencyOrderByCreatedAtDesc(agencyEntity);

        Resource resource = new ByteArrayResource(photo.getPhoto());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
