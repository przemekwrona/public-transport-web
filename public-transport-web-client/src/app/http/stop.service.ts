import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

export class Stop {
  id: string
  stopId: string;
  stopCode: string;
  stopName: string;
  stopDesc: string;
  stopLat: number;
  stopLon: number;
  locationType: number;
  parentStation: string;

  lines: string[];
  types: number[];
}

export class Stops {
  stops: Stop[]
}

@Injectable({
  providedIn: 'root'
})
export class StopService {

  constructor(private httpClient: HttpClient) {
  }

  getStops(latitude: number, longitude: number): Observable<Stops> {
    return this.httpClient.get<Stops>(`/api/agency/WAWA/stops?latitude=${latitude}&longitude=${longitude}`);
  }
}
