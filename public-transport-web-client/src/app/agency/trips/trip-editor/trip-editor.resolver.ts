import { inject } from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import { TripsService } from "../trips.service";
import {Observable} from "rxjs";
import {TripMode, Trips, TripsDetails} from "../../../generated/public-transport";

export const tripEditorResolver: ResolveFn<Observable<TripsDetails>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<TripsDetails> => {
    const line: string = route.queryParams['line'];
    const name: string = route.queryParams['name'];
    const variant: string = route.queryParams['variant'];
    const mode: TripMode = route.queryParams['mode'];

    return inject(TripsService).getTripsByVariant(line, name, variant, mode);
}