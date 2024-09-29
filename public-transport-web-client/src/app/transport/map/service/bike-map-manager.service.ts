import {Injectable} from '@angular/core';
import {GbfsService} from "../../../http/gbfs.service";
import {Marker} from "leaflet";
import {Station} from "gbfs-typescript-types/v3.0/station_information";
import {v3} from "gbfs-typescript-types";
import * as L from "leaflet";
import {map, Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class BikeMapManagerService {

    private stations: { [station_id: string]: Station };

    constructor(private gbfsService: GbfsService) {
        this.getStationInformation().subscribe((stationInformation: { [station_id: string]: Station }) => {
            this.stations = stationInformation;
        });
    }

    public getStationInformation(): Observable<{ [station_id: string]: Station }> {
        return this.gbfsService.getStationInformation('nextbike_vw');
    }

    public getBikeStationStatus(city: string): Observable<Marker[]> {
        return this.gbfsService.getStationStatus('nextbike_vw')
            .pipe(map((stationStatus: v3.StationStatus) => {
                return stationStatus.data.stations.map(stationStatus => {
                    let iconBike = L.divIcon({
                        // html: `<mat-icon matBadge="15">home</mat-icon>`,
                        html: `<div style="display: inline-flex; opacity: 0.5;"><img src="assets/warsaw/veturilo.png" width="20" height="20" style="border-radius: 4px"/><div class="num" style="background-color: #ED1C24;color:white;padding: 0 4px;border-radius: 12px;position: absolute;bottom: 4px;left: 14px;border: 1px solid red;font-size:0.5rem;">${stationStatus['num_bikes_available'] || 0}</div></div>`,
                        className: 'bike-station-marker'
                    });

                    if (stationStatus['num_bikes_available'] > 0) {
                        iconBike = L.divIcon({
                            html: `<div style="display: inline-flex"><img src="assets/warsaw/veturilo.png" width="20" height="20" style="border-radius: 4px"/><div class="num" style="background-color: #ED1C24;color:white;padding: 0 4px;border-radius: 12px;position: absolute;bottom: 4px;left: 14px;border: 1px solid red;font-size:0.5rem;">${stationStatus['num_bikes_available'] || 0}</div></div>`,
                            className: 'bike-station-marker'
                        });
                    }

                    if (stationStatus['num_bikes_available'] > 2) {
                        iconBike = L.divIcon({
                            html: `<div style="display: inline-flex"><img src="assets/warsaw/veturilo.png" width="20" height="20" style="border-radius: 4px"/><div class="num" style="background-color: yellow;padding: 0 4px;border-radius: 12px;position: absolute;bottom: 4px;left: 14px;border: 2px solid yellow;font-size:0.5rem;">${stationStatus['num_bikes_available'] || 0}</div></div>`,
                            className: 'bike-station-marker'
                        })
                    }

                    if (stationStatus['num_bikes_available'] > 4) {
                        iconBike = L.divIcon({
                            html: `<div style="display: inline-flex"><img src="assets/warsaw/veturilo.png" width="20" height="20" style="border-radius: 4px"/><div class="num" style="background-color: #2D9127;color:white;padding: 0 4px;border-radius: 12px;position: absolute;bottom: 4px;left: 14px;font-size:0.5rem;">${stationStatus['num_bikes_available'] || 0}</div></div>`,
                            className: 'bike-station-marker'
                        })
                    }

                    return L.marker([(this.stations[stationStatus.station_id])?.lat || 0.0, (this.stations[stationStatus.station_id])?.lon || 0.0], {icon: iconBike});
                });
            }));
    }
}
