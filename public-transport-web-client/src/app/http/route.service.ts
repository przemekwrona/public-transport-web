import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Stops} from "./stop.service";

export class Route {
  routeId: string;
  agencyId: string;
  routeShortName: string;
  routeLongName: string;
  routeDesc: string;
  routeType: number;
  routeUrl: string;
  routeColor: string;
}

export class Routes {
  routes: Route[];
}

@Injectable({
  providedIn: 'root'
})
export class RouteService {

  constructor(private httpClient: HttpClient) {
  }

  getRoutesByStopId(stopId: string): Observable<Routes> {
    return this.httpClient.get<Routes>(`/api/agency/WAWA/stops/${stopId}/routes`);
  }
}
