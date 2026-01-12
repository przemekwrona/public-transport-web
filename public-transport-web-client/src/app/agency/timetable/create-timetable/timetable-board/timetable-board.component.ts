import {Component, Input, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {
    AbstractControl,
    FormArray, FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule, Validators,
} from "@angular/forms";
import moment, {Moment} from "moment";
import {NgxMaterialTimepickerModule} from "ngx-material-timepicker";
import {faClock, IconDefinition} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {
    TimetablePayload,
    TimetableStopTime,
    TimetableTrip
} from "../../../../generated/public-transport-api";
import {size} from "lodash";

@Component({
    selector: 'app-timetable-board',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        NgxMaterialTimepickerModule,
        FontAwesomeModule
    ],
    templateUrl: './timetable-board.component.html',
    styleUrl: './timetable-board.component.scss'
})
export class TimetableBoardComponent implements OnInit {

    @Input() group!: FormGroup;
    @Input() submitted: boolean = false;

    @Input()
    set timetablePayload(timetablePayload: TimetablePayload | null) {
        if (timetablePayload !== this._timetablePayload) {
            this.group.get("startTime").setValue(timetablePayload.startDate);
            this.group.get("endTime").setValue(timetablePayload.endDate);
            this.group.get("interval").setValue(timetablePayload.interval);

            this.controlDepartures.setValue([]);

            for (const departure of timetablePayload.departures) {
                const departureControl: FormGroup = this.buildDeparture(departure);
                this.controlDepartures.push(departureControl);
            }

            this._timetablePayload = timetablePayload;
        }
    }

    get timetablePayload(): TimetablePayload {
        return this._timetablePayload || {};
    }

    private _timetablePayload: TimetablePayload;

    get controlDepartures(): FormArray<FormGroup> {
        return this.group.get('departures') as FormArray<FormGroup>;
    }

    public faClock: IconDefinition = faClock;

    public hours: number[] = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]

    constructor(private formBuilder: FormBuilder) {
    }

    ngOnInit(): void {
        this.mapDepartures([], true);
    }

    public mapDepartures(generatedDepartures: Moment[], appendEmpty: boolean = false) {
        for (const departure of generatedDepartures) {
            const departureControl: FormGroup = this.buildDepartureControl(departure.hour(), departure.minutes());
            this.controlDepartures.push(departureControl);
        }

        if (appendEmpty) {
            for (const hour of this.hours) {
                this.controlDepartures.push(this.buildEmptyDepartureControl(hour));
            }
        }
    }

    private buildDeparture(departure: TimetableStopTime): FormGroup {
        const [hours, minutes] = departure.time.split(':').map(Number);
        return this.buildDepartureControl(hours, minutes, departure.designation);
    }

    private buildEmptyDepartureControl(hour: number, symbol: string = '') {
        return this.buildDepartureControl(hour, null, symbol);
    }

    private buildDepartureControl(hour: number, minutes: number | null, symbol: string = '') {
        const departureControl: FormGroup = this.formBuilder.group({
            hour: [hour, [Validators.min(0), Validators.max(24)]],
            minutes: [minutes, [Validators.min(0), Validators.max(59)]],
            symbol: [symbol, []]
        });

        departureControl.get('minutes')?.valueChanges.subscribe(value => {
            this.addEmptyDepartureInHour(hour);
        });

        return departureControl;
    }

    public addEmptyDepartureInHour(hour: number) {
        const hasEmptyDeparture: boolean = this.controlDepartures.controls
            .filter((group: FormGroup): boolean => group.get("hour").value === hour)
            .map((group: FormGroup): AbstractControl => group.get("minutes"))
            .filter((minuteControl: AbstractControl): boolean => minuteControl.value == null).length > 0;

        if (!hasEmptyDeparture) {
            this.controlDepartures.push(this.buildEmptyDepartureControl(hour));
        }
    }

    public sortDepartures(): void {
        this.controlDepartures.controls.sort((a: FormGroup, b: FormGroup): number => {
            if (a.get("minutes").value == null) {
                return 1;
            }
            if (b.get("minutes").value == null) {
                return -1;
            }
            return a.get("minutes").value - b.get("minutes").value;
        });
    }

    public timesBetween(start: Moment, end: Moment, intervalMinutes = 30): Moment[] {
        const result = [];
        const cur = start.clone();

        while (cur <= end) {
            result.push(cur.clone());
            cur.add(intervalMinutes, "minutes");
        }
        return result;
    }

    public generateTimetable(modelForm: FormGroup): void {
        const start: Moment = moment(modelForm.controls['startTime'].value, 'HH:mm');
        const end: Moment = moment(modelForm.controls['endTime'].value, 'HH:mm');
        const intervalInMinutes: number = modelForm.controls['interval'].value;

        const times: Moment[] = this.timesBetween(start, end, intervalInMinutes);

        this.controlDepartures.clear();
        this.mapDepartures(times, true);
    }

    public findTripDesignation(): TimetableTrip[] {
        return (this.timetablePayload?.departures || []).filter((trip: TimetableTrip): boolean => trip.tripId.variantName !== 'MAIN');
    }

    public hasTripDesignation(): boolean {
        return size(this.findTripDesignation()) > 0;
    }

}
