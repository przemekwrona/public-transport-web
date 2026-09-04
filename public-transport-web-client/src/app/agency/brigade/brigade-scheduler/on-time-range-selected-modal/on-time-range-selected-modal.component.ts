import {Component, inject} from '@angular/core';
import {TripVariantSelectComponent} from "../trip-variant-select/trip-variant-select.component";
import {FormatSecondsPipe} from "../trip-variant-select/format-seconds.pipe";
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent, MatDialogRef,
    MatDialogTitle
} from "@angular/material/dialog";
import {
    GetAllTripsResponse,
    ProfileShortcut,
    Trip,
    TripId1,
    TripService
} from "../../../../generated/public-transport-api";
import {CommonModule} from "@angular/common";
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import moment from "moment";

export interface OnTimeRangeAndTripSelected {
    start: string
    end: string
    resourceId: string
    tripId: TripId1
    origin: string
    destination: string
}

export interface TripProfileChoice {
    trip: Trip;
    profile: ProfileShortcut;
}

@Component({
    selector: 'app-on-time-range-selected-modal',
    imports: [
        CommonModule,
        FormatSecondsPipe,
        MatDialogContent,
        MatDialogTitle,
        FormsModule,
        MatButton,
        MatDialogActions,
        MatDialogClose,
        ReactiveFormsModule
    ],
    templateUrl: './on-time-range-selected-modal.component.html',
    styleUrl: './on-time-range-selected-modal.component.scss'
})
export class OnTimeRangeSelectedModalComponent {

    private formBuilder: FormBuilder = inject(FormBuilder);

    data = inject<{ start: string, end: string, resourceId: string, defaultRoutes: GetAllTripsResponse }>(MAT_DIALOG_DATA);

    modelForm = this.formBuilder.group({
        tripId: this.formBuilder.control<TripId1 | null>(null, [Validators.required])
    });

    private selectedTrip: Trip | null = null;
    private selectedProfile: ProfileShortcut | null = null;

    constructor(private dialogRef: MatDialogRef<OnTimeRangeSelectedModalComponent>, private agencyStorageService: AgencyStorageService, private tripService: TripService) {
    }

    public selectProfile(choice: TripProfileChoice): void {
        this.selectedTrip = choice.trip;
        this.selectedProfile = choice.profile;
        this.modelForm.patchValue({
            tripId: {
                ...choice.trip.tripId,
                trafficMode: choice.profile.trafficMode
            }
        });

        this.selectTrip();
    }

    public selectTrip() {
        const tripId = this.modelForm.controls.tripId.value;
        if (!tripId) {
            return;
        }

        const selectedTrip = this.selectedTrip?.tripId?.tripCode === tripId.tripCode
            ? this.selectedTrip
            : this.findTripByTripId(tripId);
        const travelTime = (this.selectedTrip?.tripId?.tripCode === tripId.tripCode
                ? this.selectedProfile?.travelTime
                : undefined)
            ?? selectedTrip?.travelTimeInSeconds
            ?? 0;

        if (travelTime > 0 || selectedTrip) {
            this.closeWithSelection(tripId, selectedTrip, travelTime);
            return;
        }

        const instance = this.agencyStorageService.getInstance();
        const routeCode = tripId.routeId?.routeCode;
        const tripCode = tripId.tripCode;
        this.tripService.getTripVariantDetails(instance, routeCode, tripCode).subscribe(tripDetails => {
            const lastStop = tripDetails?.tripProfiles[0]?.stops?.reduce((curr, next) =>
                curr.calculatedSeconds > next.calculatedSeconds ? curr : next);

            this.closeWithSelection(tripId, selectedTrip, lastStop?.calculatedSeconds ?? 0, tripDetails?.tripId);
        });
    }

    private closeWithSelection(tripId: TripId1, trip: Trip | null, travelTime: number, fallbackTripId?: TripId1): void {
        const results: OnTimeRangeAndTripSelected = {} as OnTimeRangeAndTripSelected;
        results.start = this.data.start;
        results.end = moment(this.data.start).add(travelTime, 'seconds').format("yyyy-MM-DDTHH:mm:SS");
        results.resourceId = this.data.resourceId;
        results.tripId = tripId ?? fallbackTripId;
        results.origin = trip?.origin;
        results.destination = trip?.destination;
        this.dialogRef.close(results);
    }

    private findTripByTripId(tripId: TripId1): Trip | null {
        for (const route of this.data.defaultRoutes?.lines ?? []) {
            const trip = route.trips?.find(item => item.tripId?.tripCode === tripId.tripCode);
            if (trip) {
                return trip;
            }
        }
        return null;
    }

}
