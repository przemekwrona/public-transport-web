import {Injectable} from '@angular/core';
import {AbstractControl, AsyncValidatorFn, FormGroup} from '@angular/forms';
import {Observable, of, timer} from 'rxjs';
import {catchError, map, switchMap} from 'rxjs/operators';
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {
    RouteId,
    Status,
    TripExistence,
    TripExistenceResult,
    TripId,
    TripMode,
    TripService
} from "../../../../generated/public-transport-api";

export interface CurrentTripSelection {
    routeId?: RouteId;
    designation?: string;
    tripMode?: TripMode;
    isMainVariant?: boolean;
}

export interface TripExistenceContext {
    routeId: RouteId | undefined;
    tripId?: TripId;
    current?: CurrentTripSelection;
}

@Injectable({providedIn: 'root'})
export class TripIdExistenceValidator {

    constructor(private agencyStorageService: AgencyStorageService, private tripService: TripService) {
    }

    variantExistsValidator(context: () => TripExistenceContext): AsyncValidatorFn {
        return (control: AbstractControl): Observable<{ variantExists: boolean } | null> => {
            const form = control as FormGroup;
            const isMainVariant = !!form.get('isMainVariant')?.value;
            const designation = isMainVariant ? '' : `${form.get('variantDesignation')?.value ?? ''}`.trim();
            const tripMode = form.get('tripVariantMode')?.value as TripMode | undefined;
            const {routeId, tripId, current} = context();
            if (tripId && !current) {
                return of(null);
            }
            if (this.sameAsCurrent(current, {routeId, designation, tripMode, isMainVariant})) {
                return of(null);
            }
            if (!isMainVariant && !designation) {
                return of(null);
            }
            if (!routeId?.routeCode || !tripMode) {
                return of(null);
            }

            const tripExistence: TripExistence = {
                routeId,
                isMainVariant,
                variantDesignation: designation,
                tripMode,
                tripCode: tripId?.tripCode
            };

            return timer(400).pipe(
                switchMap(() => this.tripService.hasVariantDetails(this.agencyStorageService.getInstance(), tripExistence)),
                map((response: TripExistenceResult) => this.toFormError(response)),
                catchError(() => of(null))
            );
        };
    }

    private sameAsCurrent(current: CurrentTripSelection | undefined, selected: CurrentTripSelection): boolean {
        if (!current?.tripMode || !selected.tripMode || current.tripMode !== selected.tripMode) {
            return false;
        }
        if (!!current.isMainVariant !== !!selected.isMainVariant) {
            return false;
        }
        if (!this.sameRoute(current.routeId, selected.routeId)) {
            return false;
        }
        const currentDesignation = current.isMainVariant ? '' : `${current.designation ?? ''}`.trim();
        return currentDesignation === (selected.designation ?? '');
    }

    private sameRoute(current: RouteId | undefined, selected: RouteId | undefined): boolean {
        if (current?.routeCode && selected?.routeCode) {
            return current.routeCode === selected.routeCode;
        }
        return !!current?.line && current.line === selected?.line
            && !!current?.name && current.name === selected?.name
            && current.version === selected?.version;
    }

    private toFormError(response: TripExistenceResult): { variantExists: boolean } | null {
        const status = response?.status?.status ?? response?.status;
        if (status === Status.StatusEnum.Exists) {
            return {variantExists: true};
        }
        return null;
    }
}
