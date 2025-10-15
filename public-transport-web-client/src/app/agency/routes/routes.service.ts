import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {Route, Routes, Status, UpdateRouteRequest} from "../../generated/public-transport-api";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class RoutesService {

  constructor(private httpClient: HttpClient) { }

  public updateRoute(updateRouteRequest: UpdateRouteRequest): Observable<Status> {
    return this.httpClient.put<Status>(`/api/v1/routes`, updateRouteRequest)
  }
}
