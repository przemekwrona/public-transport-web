import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {Route, Routes, Status, UpdateRouteRequest} from "../../generated/public-transport";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class RoutesService {

  constructor(private httpClient: HttpClient) { }

  public createRoute(route: Route): Observable<Status> {
    return this.httpClient.post<Status>(`/api/v1/routes`, route);
  }

  public getRoutes(): Observable<Routes> {
    return this.httpClient.get<Routes>(`/api/v1/routes`);
  }

  public updateRoute(updateRouteRequest: UpdateRouteRequest): Observable<Status> {
    return this.httpClient.put<Status>(`/api/v1/routes`, updateRouteRequest)
  }
}
