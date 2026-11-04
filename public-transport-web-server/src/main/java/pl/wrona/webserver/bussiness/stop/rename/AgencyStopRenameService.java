package pl.wrona.webserver.bussiness.stop.rename;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyStopNameRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.StopAgencyEntity;
import pl.wrona.webserver.core.agency.StopAgencyHistoryEntity;
import pl.wrona.webserver.core.agency.StopAgencyHistoryRepository;
import pl.wrona.webserver.core.agency.StopAgencyRepository;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AgencyStopRenameService {

    private static final int MAX_NAME_LENGTH = 150;

    private final AgencyService agencyService;
    private final StopService stopService;
    private final StopAgencyRepository stopAgencyRepository;
    private final StopAgencyHistoryRepository stopAgencyHistoryRepository;

    @Transactional
    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER
    })
    public Status updateAgencyStopName(String instance, Long stopId, AgencyStopNameRequest request) {
        String name = normalize(request == null ? null : request.getName());
        AgencyEntity agency = agencyService.findAgencyByAgencyCode(instance);
        if (agency == null) {
            throw new BusinessException("ERROR:202611031701", "Agency " + instance + " does not exist");
        }
        StopEntity stop = stopService.findStopByIdsIn(List.of(stopId)).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("ERROR:202611031702", "Stop " + stopId + " does not exist"));

        LocalDateTime now = LocalDateTime.now();
        stopAgencyRepository.findByAgency_AgencyIdAndStop_StopId(agency.getAgencyId(), stopId)
                .ifPresentOrElse(current -> rename(current, agency, stop, name, now), () -> create(agency, stop, name, now));

        return new Status().status(Status.StatusEnum.SUCCESS);
    }

    private void rename(StopAgencyEntity current, AgencyEntity agency, StopEntity stop, String name, LocalDateTime now) {
        if (name.equals(current.getName())) {
            return;
        }
        LocalDateTime validFrom = current.getUpdatedAt() != null ? current.getUpdatedAt() : current.getCreatedAt();
        if (validFrom == null || !validFrom.isBefore(now)) {
            validFrom = now.minusSeconds(1);
        }
        stopAgencyHistoryRepository.save(history(agency, stop, current.getName(), current.getLon(), current.getLat(), validFrom, now));
        current.setName(name);
        current.setUpdatedAt(now);
        stopAgencyRepository.save(current);
    }

    private void create(AgencyEntity agency, StopEntity stop, String name, LocalDateTime now) {
        if (name.equals(stop.getName())) {
            return;
        }
        if (stop.getName() != null && !stop.getName().isBlank()) {
            stopAgencyHistoryRepository.save(history(
                    agency, stop, stop.getName(), stop.getLon(), stop.getLat(), now.minusSeconds(1), now));
        }
        StopAgencyEntity created = new StopAgencyEntity();
        created.setAgency(agency);
        created.setStop(stop);
        created.setName(name);
        created.setLon(stop.getLon());
        created.setLat(stop.getLat());
        created.setCreatedAt(now);
        created.setUpdatedAt(now);
        stopAgencyRepository.save(created);
    }

    private static StopAgencyHistoryEntity history(AgencyEntity agency, StopEntity stop, String name, double lon, double lat, LocalDateTime validFrom, LocalDateTime validTo) {
        StopAgencyHistoryEntity history = new StopAgencyHistoryEntity();
        history.setAgency(agency);
        history.setStop(stop);
        history.setName(name);
        history.setLon(lon);
        history.setLat(lat);
        history.setValidFrom(validFrom);
        history.setValidTo(validTo);
        return history;
    }

    private static String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("ERROR:202611031703", "Stop name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new BusinessException("ERROR:202611031704", "Stop name is longer than " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }
}
