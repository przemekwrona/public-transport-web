import {inject} from '@angular/core';
import {ResolveFn} from '@angular/router';
import {Observable, switchMap} from 'rxjs';
import {
    BrigadeService,
    GetAllTripsResponse,
    GetBrigadeDetailsResponse,
    TripService
} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';

export const defaultRouteResolver: ResolveFn<Observable<GetAllTripsResponse>> = (route, state) => {
    const brigadeService: BrigadeService = inject(BrigadeService);
    const tripService: TripService = inject(TripService);
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    const instance: string = agencyStorageService.getInstance();
    const brigadeCode: string = route.paramMap.get('brigadeCode');

    return brigadeService.getBrigadeDetails(instance, brigadeCode).pipe(
        switchMap((brigadeDetails: GetBrigadeDetailsResponse) => {
            const routeCode: string = brigadeDetails.brigade.defaultRouteCode;
            return tripService.getTripsByRouteAndFilterLineOrName(instance, routeCode);
        })
    );
};
