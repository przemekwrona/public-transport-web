import { Injectable } from '@angular/core';
import {StopsResponse, StopsService} from "../../generated/public-transport-api";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class StopService {

  constructor(private httpClient: HttpClient) {
  }

  getStopsInArea(maxLat: number, minLon: number, minLat: number, maxLon: number): Observable<StopsResponse> {
    return this.httpClient.get(`/api/v1/stops?maxLat=${maxLat}&minLon=${minLon}&minLat=${minLat}&maxLon=${maxLon}`);
  }
}
