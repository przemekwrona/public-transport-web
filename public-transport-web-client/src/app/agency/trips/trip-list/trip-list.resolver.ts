import { inject } from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import { TripsService } from "../trips.service";
import {Observable} from "rxjs";
import {Trips} from "../../../generated/public-transport";

export const tripsResolver: ResolveFn<Observable<Trips>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Trips> => {
    const line: string = route.queryParams['line'];
    const name: string = route.queryParams['name'];
    return inject(TripsService).getTrips(line, name);
}