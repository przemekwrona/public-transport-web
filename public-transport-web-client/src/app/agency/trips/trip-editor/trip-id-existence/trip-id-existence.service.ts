import {Injectable} from '@angular/core';
import {AbstractControl, AsyncValidatorFn, FormGroup, ValidatorFn} from '@angular/forms';
import {delay, Observable, of} from 'rxjs';
import {map, catchError, switchMap} from 'rxjs/operators';
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {RouteId, Status, TrafficMode, TripId, TripMode, TripService} from "../../../../generated/public-transport-api";

@Injectable({providedIn: 'root'})
export class TripIdExistenceValidator {
    constructor(private agencyStorageService: AgencyStorageService, private tripService: TripService) {
    }

    variantExistsValidator(line: string, name: string, variantName: string, tripMode: TripMode, trafficMode: TrafficMode): AsyncValidatorFn {
        return (control: FormGroup): Observable<{ variantExists: boolean } | null> => {

            const variantNameControl: AbstractControl = control.get('tripVariantName');
            const variantModeControl: AbstractControl = control.get('tripVariantMode');
            const trafficModeControl: AbstractControl = control.get('tripTrafficMode');

            if (variantNameControl.pristine && variantModeControl.pristine && trafficModeControl.pristine) {
                return of(null);
            }

            const variantNameControlValue: string = variantNameControl?.value;
            const variantModeControlValue: TripMode = variantModeControl?.value;
            const trafficModeControlValue: TrafficMode = trafficModeControl?.value;

            // Return True if it is the same trip
            if (variantName === variantNameControlValue && tripMode === variantModeControlValue && trafficMode === trafficModeControlValue) {
                return of(null);
            }

            const instance: string = this.agencyStorageService.getInstance();

            const routeId: RouteId = {};
            routeId.line = line;
            routeId.name = name;

            const tripId: TripId = {};
            tripId.routeId = routeId;

            tripId.variantName = variantNameControlValue;
            tripId.variantMode = variantModeControlValue;
            tripId.trafficMode = trafficModeControlValue;

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