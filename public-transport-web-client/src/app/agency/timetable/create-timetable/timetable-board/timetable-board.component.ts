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
    TimetableTrip, TrafficMode, TripDepartures, TripProfile
} from "../../../../generated/public-transport-api";
import {size} from "lodash";
import {FormatSecondsPipe} from "../../../brigade/brigade-scheduler/trip-variant-select/format-seconds.pipe";

export interface TimetableBoardEvent {
    time: string;
    designation?: string;
    routeCode: string;
    tripCode: string;
    trafficMode: TrafficMode;
}

export interface AvailableTripProfile {
    routeCode: string;
    tripCode: string;
    trafficMode: TrafficMode;

    isMainVariant?: boolean;
    variantDesignation?: string;
    variantDescription?: string;

    travelTimeInSeconds: number;
}

@Component({
    selector: 'app-timetable-board',
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
    @Input() tripDepartures: TripDepartures = {};

    @Input() tripProfiles: AvailableTripProfile[] = [];

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
        const defaultProfile: AvailableTripProfile = this.getFirstProfile(this.tripProfiles);
        for (const departure of generatedDepartures) {
            const departureControl: FormGroup = this.buildDepartureControl(defaultProfile.routeCode, defaultProfile.tripCode, defaultProfile.variantDesignation, departure.hour(), departure.minutes());
            this.controlDepartures.push(departureControl);
        }

        if (appendEmpty) {
            for (const hour of this.hours) {
                this.controlDepartures.push(this.buildEmptyDepartureControl(defaultProfile.routeCode, defaultProfile.tripCode, defaultProfile.variantDesignation, hour));
            }
        }
    }

    private buildEmptyDepartureControl(routeCode: string, tripCode: string, symbol: string = '', hour: number) {
        return this.buildDepartureControl(routeCode, tripCode, symbol, hour, null);
    }

    private buildDepartureControl(routeCode: string, tripCode: string, symbol: string = '', hour: number, minutes: number | null) {
        const departureControl: FormGroup = this.formBuilder.group({
            routeCode: [routeCode, [Validators.required]],
            tripCode: [tripCode, [Validators.required]],
            symbol: [symbol, []],

            hour: [hour, [Validators.min(0), Validators.max(24)]],
            minutes: [minutes, [Validators.min(0), Validators.max(59)]],
        });

        departureControl.get('minutes')?.valueChanges.subscribe(value => {
            this.addEmptyDepartureInHour(hour);
        });

        return departureControl;
    }

    public addEmptyDepartureInHour(hour: number) {
        const defaultProfile: AvailableTripProfile = this.getFirstProfile(this.tripProfiles);
        const hasEmptyDeparture: boolean = this.controlDepartures.controls
            .filter((group: FormGroup): boolean => group.get("hour").value === hour)
            .map((group: FormGroup): AbstractControl => group.get("minutes"))
            .filter((minuteControl: AbstractControl): boolean => minuteControl.value == null).length > 0;

        if (!hasEmptyDeparture) {
            this.controlDepartures.push(this.buildEmptyDepartureControl(defaultProfile.routeCode, defaultProfile.tripCode, defaultProfile.variantDesignation, hour));
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

    public getFirstProfile(availableProfiles: AvailableTripProfile[]): AvailableTripProfile | undefined {
        return [...availableProfiles].sort((left, right) => this.compareProfiles(left, right))[0];
    }

    private compareProfiles(left: AvailableTripProfile, right: AvailableTripProfile): number {
        const mainOrder = Number(!!right.isMainVariant) - Number(!!left.isMainVariant);
        if (mainOrder !== 0) {
            return mainOrder;
        }

        const symbolOrder = (left.variantDesignation ?? '').localeCompare(right.variantDesignation ?? '');
        if (symbolOrder !== 0) {
            return symbolOrder;
        }

        return this.trafficModeOrder(left.trafficMode) - this.trafficModeOrder(right.trafficMode);
    }

    private trafficModeOrder(trafficMode: TrafficMode): number {
        if (trafficMode === TrafficMode.Normal) {
            return 0;
        }
        if (trafficMode === TrafficMode.Traffic) {
            return 1;
        }
        return 2;
    }

}
