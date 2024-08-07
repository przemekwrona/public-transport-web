import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {map, Observable} from "rxjs";
import {v3} from 'gbfs-typescript-types';
import {Data, Station} from "gbfs-typescript-types/v3.0/station_information";

@Injectable({
    providedIn: 'root'
})
export class GbfsService {

    private stationInformation: { [station_id: string]: v3.StationInformation[] };

    constructor(private httpClient: HttpClient) {
    }

    getStationStatus(region: string): Observable<v3.StationStatus> {
        return this.httpClient.get<v3.StationStatus>(`/api/public/v2/${region}/station_status.json`);
    }

    getStationInformation(region: string): Observable<{[station_id: string]: Station}> {
        return this.httpClient.get<v3.StationInformation>(`/api/public/v2/${region}/station_information.json`)
            .pipe(
                map((response: v3.StationInformation) => response.data),
                map((data: Data) => data.stations),
                map((stations: Station[]) => stations.reduce((acc: {[station_id: string]: Station}, station: Station) => {
                    acc[station.station_id] = station;
                    return acc;
                }, {})));
    }
}
