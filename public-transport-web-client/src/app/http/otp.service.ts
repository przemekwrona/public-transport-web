import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {RoutesResponse} from "../../../generated";
import moment from "moment";

@Injectable({
  providedIn: 'root'
})
export class OtpService {

  constructor(private httpClient: HttpClient) {
  }

  plan(startLat: number, startLon: number, endLat: number, endLon: number, departureDate: moment.Moment): Observable<RoutesResponse> {
    return this.httpClient.get<RoutesResponse>(`http://localhost:9082/otp/routers/default/plan?fromPlace=${startLat},${startLon}&toPlace=${endLat},${endLon}&date=${departureDate.format('yyyy-MM-dd')}&time=${departureDate.format('HH:mm')}&mode=TRANSIT&showIntermediateStops=true&local=en`);
  }
}
