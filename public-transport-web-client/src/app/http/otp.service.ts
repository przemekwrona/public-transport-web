import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import moment from "moment";
import {RoutesResponse, Stop, StopDetails, Route, StopTime} from "../generated";

@Injectable({
  providedIn: 'root'
})
export class OtpService {

  constructor(private httpClient: HttpClient) {
  }

  plan(startLat: number, startLon: number, endLat: number, endLon: number, departureDate: moment.Moment): Observable<RoutesResponse> {
    return this.httpClient.get<RoutesResponse>(`/api/otp/routers/default/plan?fromPlace=${startLat},${startLon}&toPlace=${endLat},${endLon}&date=${departureDate.format('yyyy-MM-dd')}&time=${departureDate.format('HH:mm')}&mode=TRANSIT&showIntermediateStops=true&local=en`);
  }

  planBike(startLat: number, startLon: number, endLat: number, endLon: number, departureDate: moment.Moment): Observable<RoutesResponse> {
    return this.httpClient.get<RoutesResponse>(`/api/otp/routers/default/plan?fromPlace=${startLat},${startLon}&toPlace=${endLat},${endLon}&date=${departureDate.format('yyyy-MM-dd')}&time=${departureDate.format('HH:mm')}&mode=BICYCLE_RENT&showIntermediateStops=true&local=en`);
  }

  getStops(maxLat: number, minLon: number, minLat: number, maxLon: number): Observable<Stop[]> {
      const headerDict = {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
      }

    return this.httpClient.get<Stop[]>(`/api/otp/routers/default/index/stops?maxLat=${maxLat}&minLon=${minLon}&minLat=${minLat}&maxLon=${maxLon}`, {headers: new HttpHeaders(headerDict)});
  }

  getStopDetails(stopId: string): Observable<StopDetails> {
    return this.httpClient.get<StopDetails>(`/api/otp/routers/default/index/stops/${stopId}`);
  }

  getRoutes(stopId: string): Observable<Route[]> {
    return this.httpClient.get<Route[]>(`/api/otp/routers/default/index/stops/${stopId}/routes`);
  }

  getStopTimes(stopId: string, date: moment.Moment): Observable<StopTime[]> {
    return this.httpClient.get<StopTime[]>(`/api/otp/routers/default/index/stops/${stopId}/stoptimes/${date.format('yyyyMMDD')}`);
  }
}
