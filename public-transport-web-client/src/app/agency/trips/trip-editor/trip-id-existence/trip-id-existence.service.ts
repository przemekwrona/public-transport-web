import {Injectable} from '@angular/core';
import {AbstractControl, AsyncValidatorFn, FormGroup} from '@angular/forms';
import {delay, Observable, of} from 'rxjs';
import {map, catchError, switchMap} from 'rxjs/operators';
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {RouteId, Status, TrafficMode, TripId, TripMode, TripService} from "../../../../generated/public-transport-api";

@Injectable({providedIn: 'root'})
export class TripIdExistenceValidator {
    constructor(private agencyStorageService: AgencyStorageService, private tripService: TripService) {
    }

    variantExistsValidator(routeLine: string, routeName: string, variantNameKey: AbstractControl, variantModeKey: AbstractControl, trafficModeKey: AbstractControl): AsyncValidatorFn {
        return (control: FormGroup): Observable<{ variantExists: boolean } | null> => {
            // Return null for empty values (valid by default)
            if (!control.value) {
                return of(null);
            }

            if (variantNameKey.pristine && variantModeKey.pristine && trafficModeKey.pristine) {
                return of(null);
            }

            const variantName: string = variantNameKey?.value;
            const variantMode: TripMode = variantModeKey?.value;
            const trafficMode: TrafficMode = trafficModeKey?.value;

            const instance: string = this.agencyStorageService.getInstance();

            const routeId: RouteId = {};
            routeId.line = routeLine;
            routeId.name = routeName;

            const tripId: TripId = {};
            tripId.routeId = routeId;

            tripId.variantName = variantName;
            tripId.variantMode = variantMode;
            tripId.trafficMode = trafficMode;

            return of(control.value).pipe(
                // Delay processing to debounce user input
                delay(3 * 1000),
                // debounceTime(3 * 1000),
                // Cancel previous requests and switch to the latest
                switchMap((value): Observable<{ variantExists: boolean } | null> =>
                    this.tripService.hasVariantDetails(instance, tripId).pipe(
                        // If the API returns data, emit { emailExists: true }; otherwise null
                        map((response: Status): {
                            variantExists: boolean
                        } => (response.status === Status.StatusEnum.Exists ? {variantExists: true} : null)),
                        // Handle errors (e.g., network issues)
                        catchError((): Observable<null> => of(null))
                    )
                )
            );
        };
    }
}