import { Injectable } from '@angular/core';
import moment from "moment";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {JourneySummaryResponse} from "../generated/public-transport-api";

@Injectable({
  providedIn: 'root'
})
export class JourneySummaryService {

  constructor(private httpClient: HttpClient) {
  }

  summary(startLat: number, startLon: number, endLat: number, endLon: number, departureDate: moment.Moment): Observable<JourneySummaryResponse> {
    return this.httpClient.get<JourneySummaryResponse>(`/api/otp/routers/default/plan/summary?fromPlace=${startLat},${startLon}&toPlace=${endLat},${endLon}&date=${departureDate.format('yyyy-MM-dd')}&time=${departureDate.format('HH:mm')}&mode=TRANSIT&showIntermediateStops=true&local=en`);
  }
}
