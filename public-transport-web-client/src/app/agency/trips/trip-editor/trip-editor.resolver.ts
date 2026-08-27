import {inject} from "@angular/core";
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from "@angular/router";
import {Observable, of} from "rxjs";
import {
    TrafficMode,
    TripMode, TripProfile,
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
            tripId: {
                routeId: {
                    routeCode: routeCode
                },
                variant: '',
                mode: TripMode.Front,
                trafficMode: TrafficMode.Normal
            },
            isMainVariant: true,
            isCustomized: false,
            tripProfiles: [
                {
                    trafficMode: TrafficMode.Normal,
                    travelTimeInSeconds: 0,
                    calculatedCommunicationVelocity: 40,
                    customizedCommunicationVelocity: 40,
                    isDefault: true,
                    isCustomized: false,
                    stops: []
                } as TripProfile
            ]
        } as TripsDetails)
    } else {
        const tripService: TripService = inject(TripService);
        return tripService.getTripVariantDetails(agencyStorageService.getInstance(), routeCode, tripCode);
    }
}