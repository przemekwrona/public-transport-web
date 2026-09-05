import {inject} from '@angular/core';
import {ResolveFn} from '@angular/router';
import {Observable, switchMap} from 'rxjs';
import {
    BrigadeService,
    GetAllTripsResponse,
    GetBrigadeDetailsResponse,
    Trip,
    TripService,
    TripsDetails
} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';

export const defaultTripResolver: ResolveFn<Observable<TripsDetails>> = (route, state) => {
    const brigadeService: BrigadeService = inject(BrigadeService);
    const tripService: TripService = inject(TripService);
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    const instance: string = agencyStorageService.getInstance();
    const brigadeCode: string = route.paramMap.get('brigadeCode');

    return brigadeService.getBrigadeDetails(instance, brigadeCode).pipe(
        switchMap((brigadeDetails: GetBrigadeDetailsResponse) => {
            const routeCode: string = brigadeDetails.brigade.defaultRouteCode;
            return tripService.getTripsByRouteAndFilterLineOrName(instance, routeCode).pipe(
                switchMap((tripsResponse: GetAllTripsResponse) => {
                    const trips: Trip[] = tripsResponse.lines?.[0]?.trips ?? [];
                    const defaultTrip: Trip = trips.find(trip => trip.isMainVariant) ?? trips[0];
                    const tripCode: string = defaultTrip?.tripId?.tripCode;
                    return tripService.getTripVariantDetails(instance, routeCode, tripCode);
                })
            );
        })
    );
};
