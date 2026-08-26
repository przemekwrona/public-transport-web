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
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);
    const routeCode: string = route.params['routeCode'];
    const tripCode: string = route.params['tripCode'];

    const tripEditorComponentMode: TripEditorComponentMode = route.data['mode'];

    if (tripEditorComponentMode === TripEditorComponentMode.CREATE) {
        return of({
            tripId: {variant: '', mode: TripMode.Front, trafficMode: TrafficMode.Normal},
            isMainVariant: true,
            isCustomized: false,
            item: {stops: [], isMainVariant: false}} as TripsDetails)
    } else {
        const tripService: TripService = inject(TripService);
        return tripService.getTripVariantDetails(agencyStorageService.getInstance(), routeCode, tripCode);
    }
}