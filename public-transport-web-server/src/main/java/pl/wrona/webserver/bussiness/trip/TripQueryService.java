package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripQueryService {

    private final TripQueryRepository tripQueryRepository;

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public List<TripEntity> findByAgencyCodeAndLineAndName(String instance, String line, String name) {
        return tripQueryRepository.findByAgencyCodeAndLineAndName(instance, line, name);
    }

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public List<TripEntity> findByAgencyCodeAndRoute(String instance, RouteEntity route) {
        return tripQueryRepository.findByAgencyCodeAndRoute(instance, route);
    }

    public TripEntity findTripByAgencyAndRouteCodeAndTripCode(String instance, String routeCode, String tripCode) {
        return tripQueryRepository.findTripByAgencyAndRouteCodeAndTripCode(instance, routeCode, tripCode);
    }

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public List<TripEntity> findByAgencyAndRouteCodeAndVariantMode(String instance, String routeCode, TripVariantMode variantMode) {
        return tripQueryRepository.findByAgencyAndRouteCodeAndVariantMode(instance, routeCode, variantMode);
    }

    public List<TripEntity> findByExistsBrigade(String agency) {
        return tripQueryRepository.findByExistsBrigade(agency);
    }

    public Map<Long, TripEntity> mapByExistsBrigade(String agency) {
        return findByExistsBrigade(agency).stream()
                .collect(Collectors.toMap(TripEntity::getTripId, Function.identity()));
    }

    public boolean existsTripInBrigade(TripEntity trip) {
        return tripQueryRepository.existsTripInBrigade(trip);
    }

    public List<TripEntity> findByRouteMainVariantAndDesignation(String agencyCode, String routeCode, boolean mainVariant, String designation, Long excludedTripId) {
        String normalizedDesignation = mainVariant ? StringUtils.EMPTY : StringUtils.trimToEmpty(designation);
        return tripQueryRepository.findAllByAgencyAndRouteCode(agencyCode, routeCode).stream()
                .filter(trip -> trip.isMainVariant() == mainVariant)
                .filter(trip -> normalizedDesignation.equals(mainVariant ? StringUtils.EMPTY : StringUtils.trimToEmpty(trip.getVariantDesignation())))
                .filter(trip -> excludedTripId == null || !excludedTripId.equals(trip.getTripId()))
                .toList();
    }

    public void assertUniqueTrip(String agencyCode, String routeCode, boolean mainVariant, String designation, Long excludedTripId) {
        boolean exists = !findByRouteMainVariantAndDesignation(agencyCode, routeCode, mainVariant, designation, excludedTripId).isEmpty();
        if (exists) {
            throw new BusinessException("ERROR:202610251802", "Trip with this route, main variant and designation already exists");
        }
    }
}
