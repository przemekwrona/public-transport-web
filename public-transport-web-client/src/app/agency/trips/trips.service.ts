import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Route, Trips, Trip, Status} from "../../generated/public-transport";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class TripsService {

  constructor(private httpClient: HttpClient) { }

  public create(trips: Trip): Observable<Trip> {
    return this.httpClient.post<Trip>(`/api/v1/trips`, trips);
  }

  public update(trips: Trip): Observable<Status> {
    return this.httpClient.put<Status>(`/api/v1/trips`, trips);
  }

  public getTrips(name: string, line: string): Observable<Trips> {
    return this.httpClient.post<Trips>(`/api/v1/trips/details`, {name: name, line: line});
  }

  public getTripsByVariant(name: string, line: string, variant: string): Observable<Trips> {
    return this.httpClient.post<Trips>(`/api/v1/trips/variant-details`, {name: name, line: line, variant: variant});
  }

  public measureDistance(trips: Trip): Observable<Trip> {
    return this.httpClient.post<Trip>(`/api/v1/trips/measure-distance`, trips);
  }
}
