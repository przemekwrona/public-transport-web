import {inject} from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {Observable, of} from "rxjs";
import {TripId, TripMode, TripsDetails, TripService} from "../../../generated/public-transport-api";
import {TripEditorComponentMode} from "./trip-editor-component-mode";

export const tripEditorResolver: ResolveFn<Observable<TripsDetails>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<TripsDetails> => {
    const line: string = route.queryParams['line'];
    const name: string = route.queryParams['name'];
    const variant: string = route.queryParams['variant'];
    const mode: TripMode = route.queryParams['mode'];

    const tripEditorComponentMode: TripEditorComponentMode = route.data['mode'];

    if (tripEditorComponentMode === TripEditorComponentMode.CREATE) {
        return of({route: {line: line, name: name}, trip: {stops: [], line: line, name: name, isMainVariant: false}})
    } else {
        const tripService: TripService = inject(TripService);
        const tripId: TripId = {line: line, name: name, variant: variant, mode: mode} as TripId;
        return tripService.getTripByVariant(tripId);
    }
}