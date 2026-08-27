import {Component, inject} from '@angular/core';
import {TripVariantSelectComponent} from "../trip-variant-select/trip-variant-select.component";
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent, MatDialogRef,
    MatDialogTitle
} from "@angular/material/dialog";
import {TripId, TripId1, TripService} from "../../../../generated/public-transport-api";
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

@Component({
    selector: 'app-on-time-range-selected-modal',
    imports: [
        CommonModule,
        TripVariantSelectComponent,
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

    data = inject<{ start: string, end: string, resourceId: string }>(MAT_DIALOG_DATA);

    modelForm = this.formBuilder.group({
        tripId: [null, [Validators.required]]
    });

    constructor(private dialogRef: MatDialogRef<OnTimeRangeSelectedModalComponent>, private agencyStorageService: AgencyStorageService, private tripService: TripService) {
    }

    public selectTrip() {
        const instance = this.agencyStorageService.getInstance();
        const tripId: TripId = this.modelForm.controls['tripId'].value;
        this.tripService.getTripVariantDetails(instance, 'tripId', 'tripId').subscribe(tripDetails => {

            const lastStop = tripDetails?.tripProfiles[0]?.stops?.reduce((curr, next) =>
                curr.calculatedSeconds > next.calculatedSeconds ? curr : next);

            const results: OnTimeRangeAndTripSelected = {} as OnTimeRangeAndTripSelected;
            results.start = this.data.start;
            results.end = moment(this.data.start).add(lastStop.calculatedSeconds, 'seconds').format("yyyy-MM-DDTHH:mm:SS");
            results.resourceId = this.data.resourceId;
            results.tripId = tripId;

            this.dialogRef.close(results);
        });
    }

}
