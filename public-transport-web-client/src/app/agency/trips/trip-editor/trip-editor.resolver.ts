import {inject} from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {TripsService} from "../trips.service";
import {Observable, of} from "rxjs";
import {TripMode, TripsDetails} from "../../../generated/public-transport";
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
        return inject(TripsService).getTripsByVariant(line, name, variant, mode);
    }
}