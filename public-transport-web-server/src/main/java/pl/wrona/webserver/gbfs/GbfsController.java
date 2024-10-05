package pl.wrona.webserver.gbfs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.igeolab.iot.gbfs.server.api.ApiV2Api;
import org.igeolab.iot.gbfs.server.api.model.FreeBikeStatusV23;
import org.igeolab.iot.gbfs.server.api.model.GbfsV23;
import org.igeolab.iot.gbfs.server.api.model.GbfsVersionsV23;
import org.igeolab.iot.gbfs.server.api.model.GeofencingZonesV23;
import org.igeolab.iot.gbfs.server.api.model.StationInformationV23;
import org.igeolab.iot.gbfs.server.api.model.StationStatusV23;
import org.igeolab.iot.gbfs.server.api.model.SystemInformationV23;
import org.igeolab.iot.gbfs.server.api.model.SystemPricingPlansV23;
import org.igeolab.iot.gbfs.server.api.model.VehicleTypesV23;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class GbfsController implements ApiV2Api {

    private final GbfsService gbfsService;

    @Override
    public ResponseEntity<GbfsVersionsV23> v2GbfsVersions(String authorization) {
        return null;
    }

    @Override
    public ResponseEntity<GeofencingZonesV23> v2GeofencingZones(String authorization) {
        return null;
    }

    @Override
    public ResponseEntity<FreeBikeStatusV23> v2GlobalFreeBikeStatus(String authorization) {
        return null;
    }

    @Override
    public ResponseEntity<GbfsV23> v2GlobalGbfs(String authorization) {
        return null;
    }

    @Override
    public ResponseEntity<GbfsV23> v2GlobalGbfsPublic() {
        return null;
    }

    @Override
    public ResponseEntity<GbfsVersionsV23> v2GlobalGbfsVersionsPublic() {
        return null;
    }

    @Override
    public ResponseEntity<StationInformationV23> v2GlobalStationInformation(String authorization) {
        return null;
    }

    @Override
    public ResponseEntity<StationStatusV23> v2GlobalStationStatus(String authorization) {
        return null;
    }

    @Override
    public ResponseEntity<VehicleTypesV23> v2GlobalVehicleTypes(String authorization) {
        return null;
    }

    @Override
    public ResponseEntity<FreeBikeStatusV23> v2RegionalFreeBikeStatus(String authorization, String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<FreeBikeStatusV23> v2RegionalFreeBikeStatusPublic(String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<GbfsV23> v2RegionalGbfs(String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<GbfsV23> v2RegionalGbfsPublic(String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<GbfsVersionsV23> v2RegionalGbfsVersions(String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<GbfsVersionsV23> v2RegionalGbfsVersionsPublic(String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<GeofencingZonesV23> v2RegionalGeofencingZones(String authorization, String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<GeofencingZonesV23> v2RegionalGeofencingZonesPublic(String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<StationInformationV23> v2RegionalStationInformation(String authorization, String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<StationInformationV23> v2RegionalStationInformationPublic(String regionName) {
        StationInformationV23 response = this.gbfsService.v2RegionalStationInformationPublic(regionName);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<StationStatusV23> v2RegionalStationStatus(String authorization, String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<StationStatusV23> v2RegionalStationStatusPublic(String regionName) {
        StationStatusV23 response = this.gbfsService.v2RegionalStationStatusPublic(regionName);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SystemInformationV23> v2RegionalSystemInformation(String authorization, String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<SystemInformationV23> v2RegionalSystemInformationPublic(String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<SystemPricingPlansV23> v2RegionalSystemPricingPlans(String authorization, String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<SystemPricingPlansV23> v2RegionalSystemPricingPlansPublic(String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<VehicleTypesV23> v2RegionalVehicleTypes(String authorization, String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<VehicleTypesV23> v2RegionalVehicleTypesPublic(String regionName) {
        return null;
    }

    @Override
    public ResponseEntity<SystemInformationV23> v2SystemInformation(String authorization) {
        return null;
    }

    @Override
    public ResponseEntity<SystemPricingPlansV23> v2SystemPricingPlans(String authorization) {
        return null;
    }
}
