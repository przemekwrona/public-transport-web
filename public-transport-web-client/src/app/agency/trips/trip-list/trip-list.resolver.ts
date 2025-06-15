import {inject} from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";
import {RouteId, Trips, TripService} from "../../../generated/public-transport";

export const tripsResolver: ResolveFn<Observable<Trips>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Trips> => {
    const line: string = route.queryParams['line'];
    const name: string = route.queryParams['name'];
    const routeId: RouteId = {line: line, name: name} as RouteId;
    return inject(TripService).getTrips(routeId);
}