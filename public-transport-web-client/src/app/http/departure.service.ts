import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

export class Departure {
  line: string;
  headsign: string;
  date: Date;
}

export class Departures {
  departures: Departure[];
}

@Injectable({
  providedIn: 'root'
})
export class DepartureService {

  constructor(private httpClient: HttpClient) {
  }

  getDepartures(agencyId: string, stopId: string): Observable<Departures> {
    return this.httpClient.get<Departures>(`/api/agency/${agencyId}/stops/${stopId}/departures`);
  }
}
