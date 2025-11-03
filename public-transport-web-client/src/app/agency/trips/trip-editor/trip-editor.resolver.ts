import {inject} from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {Observable, of} from "rxjs";
import {
    RouteId,
    TrafficMode,
    TripId,
    TripMode,
    TripsDetails,
    TripService
} from "../../../generated/public-transport-api";
import {TripEditorComponentMode} from "./trip-editor-component-mode";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const tripEditorResolver: ResolveFn<Observable<TripsDetails>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<TripsDetails> => {
    const line: string = route.queryParams['line'];
    const name: string = route.queryParams['name'];
    const variant: string = route.queryParams['variant'];
    const mode: TripMode = route.queryParams['mode'];

    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    const tripEditorComponentMode: TripEditorComponentMode = route.data['mode'];

    if (tripEditorComponentMode === TripEditorComponentMode.CREATE) {
        return of({
            tripId: {routeId: {line: line, name: name}, variant: '', mode: TripMode.Front, trafficMode: TrafficMode.Normal},
            trip: {stops: [], line: line, name: name, isMainVariant: false}})
    } else {
        const tripService: TripService = inject(TripService);
        const routeId: RouteId = {line: line, name: name} as RouteId;
        const tripId: TripId = {routeId: routeId, variant: variant, mode: mode} as TripId;
        return tripService.getTripByVariant(agencyStorageService.getInstance(), tripId);
    }
}