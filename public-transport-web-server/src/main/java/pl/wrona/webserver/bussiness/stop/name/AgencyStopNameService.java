package pl.wrona.webserver.bussiness.stop.name;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Stop;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.StopAgencyEntity;
import pl.wrona.webserver.core.agency.StopAgencyRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AgencyStopNameService {

    private final AgencyRepository agencyRepository;
    private final StopAgencyRepository stopAgencyRepository;

    @Transactional(readOnly = true)
    public Map<Long, String> findOverrides(String agencyCode, Collection<Long> stopIds) {
        if (agencyCode == null || agencyCode.isBlank() || stopIds == null || stopIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = stopIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        AgencyEntity agency = agencyRepository.findByAgencyCodeEquals(agencyCode);
        if (agency == null || agency.getAgencyId() == null) {
            return Map.of();
        }
        return stopAgencyRepository.findByAgencyIdAndStopIds(agency.getAgencyId(), ids).stream()
                .filter(row -> row.getStop() != null && row.getStop().getStopId() != null)
                .filter(row -> row.getName() != null && !row.getName().isBlank())
                .collect(Collectors.toMap(row -> row.getStop().getStopId(), StopAgencyEntity::getName, (left, right) -> left));
    }

    public void apply(Map<Long, String> overrides, Stop... stops) {
        if (stops == null) {
            return;
        }
        for (Stop stop : stops) {
            if (stop == null) {
                continue;
            }
            stop.setName(prefer(stop.getName(), overrides, stop.getId()));
        }
    }

    public static String prefer(String canonical, Map<Long, String> overrides, Long stopId) {
        if (overrides != null && stopId != null) {
            String override = overrides.get(stopId);
            if (override != null && !override.isBlank()) {
                return override;
            }
        }
        return canonical;
    }

    public static Long stopId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(raw.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
