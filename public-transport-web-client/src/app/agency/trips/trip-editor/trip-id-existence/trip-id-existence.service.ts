import {Injectable} from '@angular/core';
import {AbstractControl, AsyncValidatorFn, FormGroup} from '@angular/forms';
import {Observable, of} from 'rxjs';
import {debounceTime, map, catchError, switchMap} from 'rxjs/operators';
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {RouteId, Status, TrafficMode, TripId, TripMode, TripService} from "../../../../generated/public-transport-api";

@Injectable({providedIn: 'root'})
export class TripIdExistenceValidator {
    constructor(private agencyStorageService: AgencyStorageService, private tripService: TripService) {
    }

    variantExistsValidator(routeLine: string, routeName: string, variantNameKey: string, variantModeKey: string, trafficModeKey: string): AsyncValidatorFn {
        return (control: FormGroup): Observable<{ variantExists: boolean } | null> => {
            // Return null for empty values (valid by default)
            // if (!control) {
            //     return of(null);
            // }
            //

            console.log(control);
            const variantName: string = control.get(variantNameKey)?.value;
            const variantMode: TripMode = control.get(variantModeKey)?.value;
            const trafficMode: TrafficMode = control.get(trafficModeKey)?.value;
            console.log(variantName);
            console.log(variantMode);
            console.log(trafficMode);
            // console.log(control.get(variantName));
            //
            // const value = control.value?.trim();
            // if (!value) return of(null);
            //
            const instance: string = this.agencyStorageService.getInstance();

            const routeId: RouteId = {};
            routeId.line = routeLine;
            routeId.name = routeName;

            const tripId: TripId = {};
            tripId.routeId = routeId;

            tripId.variantName = '';
            tripId.variantMode = TripMode.Front;
            tripId.trafficMode = TrafficMode.Normal;

            return of(control.value).pipe(
                // Delay processing to debounce user input
                debounceTime(500),
                // Cancel previous requests and switch to the latest

                switchMap((email) =>
                    this.tripService.hasVariantDetails(instance, tripId).pipe(
                        // If the API returns data, emit { emailExists: true }; otherwise null
                        map((response: Status) => (response.status === Status.StatusEnum.Exists ? {variantExists: true} : null)),
                        // Handle errors (e.g., network issues)
                        catchError(() => of(null))
                    )
                )
            );
        };
    }
}