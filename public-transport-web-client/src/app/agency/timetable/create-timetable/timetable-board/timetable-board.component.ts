import {Component, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import moment, {Moment} from "moment";
import {NgxMaterialTimepickerModule} from "ngx-material-timepicker";
import {faClock, faGlobe, faSpinner, IconDefinition} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";

export interface Departure {
    hour: number;
    minutes: number | null;
    symbol: string;
}


export interface TimetableGeneratorConfig {
    startTimeGenerator: string;
    endTimeGenerator: string;
    intervalInMinutes: number;
}

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

    public faClock: IconDefinition = faClock;
    public faGlobe: IconDefinition = faGlobe;
    public faSpinner: IconDefinition = faSpinner;

    public hours: number[] = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]

    public generatorConfig: TimetableGeneratorConfig = {} as TimetableGeneratorConfig;

    public departures: Map<number, Departure[]> = new Map<number, Departure[]>();

    public timetableGeneratorForm: FormGroup;

    constructor(private formBuilder: FormBuilder) {
    }


    ngOnInit(): void {
        this.timetableGeneratorForm = this.formBuilder.group({
            startTimePicker: ['', [Validators.required, Validators.min(0)]],
            endTimePicker: ['', [Validators.required, Validators.min(0)]],
            interval: ['', [Validators.required, Validators.min(0)]]
        });

        this.departures = this.mapDepartures([], true);

    }

    public mapDepartures(generatedDepartures: Moment[], appendEmpty: boolean = false): Map<number, Departure[]> {
        const departures: Map<number, Departure[]> = new Map<number, Departure[]>();

        for (const departure of generatedDepartures) {

            const emptyDeparture: Departure = {
                hour: departure.hours(),
                minutes: departure.minutes(),
                symbol: ""
            } as Departure;

            if (departures.get(departure.hour()) == null) {
                departures.set(departure.hour(), [emptyDeparture]);
            } else {
                departures.get(departure.hour()).push(emptyDeparture);
            }
        }

        if (appendEmpty) {
            for (const hour of this.hours) {
                const emptyDeparture: Departure = {hour: hour, minutes: null, symbol: ""} as Departure;

                if (departures.get(hour) == null) {
                    const emptyList: Departure[] = [];
                    emptyList.push(emptyDeparture);
                    departures.set(hour, emptyList);
                } else {
                    departures.get(hour).push(emptyDeparture);
                }
            }
        }

        return departures;
    }

    public addEmptyDepartureInHour(hour: number) {
        const hasEmptyDeparture: boolean = this.departures?.get(hour)
            .map(departure => departure.minutes)
            .filter(minute => minute == null).length > 0;

        if (!hasEmptyDeparture) {
            const emptyDeparture: Departure = {} as Departure;
            emptyDeparture.hour = hour;
            emptyDeparture.symbol = "";
            this.departures.get(hour).push(emptyDeparture);
        }

        const arr: Departure[] = this.departures.get(hour) ?? [];
        const sortedArr = [...arr].sort((a, b) => a.minutes - b.minutes);

        this.departures.set(hour, sortedArr);
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
        const start: Moment = moment(modelForm.controls['startTimePicker'].value, 'HH:mm');
        const end: Moment = moment(modelForm.controls['endTimePicker'].value, 'HH:mm');
        const intervalInMinutes: number = modelForm.controls['interval'].value;

        const times: Moment[] = this.timesBetween(start, end, intervalInMinutes);

        this.departures = this.mapDepartures(times, true);
    }

}
