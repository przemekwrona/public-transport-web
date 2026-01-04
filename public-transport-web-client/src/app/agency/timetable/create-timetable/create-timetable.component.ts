import {Component, OnInit} from '@angular/core';
import {TimetableBoardComponent} from "./timetable-board/timetable-board.component";
import {
    AbstractControl,
    FormArray,
    FormBuilder,
    FormControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators
} from "@angular/forms";
import {
    CreateTimetableGeneratorRequest,
    GetCalendarsResponse,
    TimetableGeneratorFilterByRoutesResponse,
    TimetableGeneratorPayload,
    TimetableGeneratorService,
    TimetablePayload,
    TimetableStopTime
} from "../../../generated/public-transport-api";
import {ActivatedRoute} from "@angular/router";
import {NgxMaterialTimepickerModule} from "ngx-material-timepicker";
import {CommonModule} from "@angular/common";
import {NgxMatSelectSearchModule} from "ngx-mat-select-search";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {RouteNameNormPipe} from "./route-id-normalization.pipe";
import moment from "moment";
import {AgencyStorageService} from "../../../auth/agency-storage.service";


export interface AllValidationErrors {
    controlName: string;
    errorName: string;
    errorValue: any;
}

export function getFormValidationErrors(
    control: AbstractControl,
    errors: AllValidationErrors[] = [],
    path: string = ''
): AllValidationErrors[] {

    if (control instanceof FormControl) {
        if (control.errors) {
            Object.entries(control.errors).forEach(([errorName, errorValue]) => {
                errors.push({
                    controlName: path,
                    errorName,
                    errorValue
                });
            });
        }
        return errors;
    }

    if (control instanceof FormGroup) {
        Object.entries(control.controls).forEach(([key, childControl]) => {
            const childPath = path ? `${path}.${key}` : key;
            getFormValidationErrors(childControl, errors, childPath);
        });
        return errors;
    }

    if (control instanceof FormArray) {
        control.controls.forEach((childControl, index) => {
            const childPath = `${path}[${index}]`;
            getFormValidationErrors(childControl, errors, childPath);
        });
        return errors;
    }

    return errors;
}


@Component({
    selector: 'app-create-timetable',
    standalone: true,
    imports: [
        CommonModule,
        TimetableBoardComponent,
        ReactiveFormsModule,
        FormsModule,
        NgxMaterialTimepickerModule,
        NgxMatSelectSearchModule,
        MatFormFieldModule,
        MatSelectModule,
        RouteNameNormPipe
    ],
    templateUrl: './create-timetable.component.html',
    styleUrl: './create-timetable.component.scss'
})
export class CreateTimetableComponent implements OnInit {

    public formGroup: FormGroup;
    public calendarsResponse: GetCalendarsResponse = {};
    public routes: TimetableGeneratorFilterByRoutesResponse = {};
    public isSubmitted: boolean = false;

    /** control for the MatSelect filter keyword */
    public bankFilterCtrl: FormControl<string> = new FormControl<string>('');

    get controlTimetables(): FormArray<FormGroup> {
        return this.formGroup.get('timetables') as FormArray<FormGroup>;
    }

    constructor(private formBuilder: FormBuilder, private _route: ActivatedRoute, private timetableGeneratorService: TimetableGeneratorService, private agencyStorageService: AgencyStorageService) {
    }

    ngOnInit(): void {
        this.formGroup = this.formBuilder.group({
            routeId: [null, [Validators.required]],
            timetables: this.formBuilder.array([this.buildTimetable()])
        });

        this._route.data.subscribe(data => this.calendarsResponse = data['calendars']);
        this._route.data.subscribe(data => this.routes = data['routes']);
    }

    private buildTimetable(): FormGroup {
        return this.formBuilder.group({
            calendarName: ['', [Validators.required]],
            front: this.formBuilder.group({
                startTime: ['06:00'],
                endTime: ['20:00'],
                interval: 15,
                departures: this.formBuilder.array([])
            }),
            back: this.formBuilder.group({
                startTime: ['06:00'],
                endTime: ['20:00'],
                interval: 18,
                departures: this.formBuilder.array([])
            })
        })
    }

    private getTimetableByName(): FormGroup {
        return this.controlTimetables.controls[0];
    }

    public getFrontTimetableByName(): FormGroup {
        return this.getTimetableByName().get("front") as FormGroup
    }

    public getBackTimetableByName(): FormGroup {
        return this.getTimetableByName().get("back") as FormGroup
    }

    public getRouteIdFormControl(): FormControl {
        return this.formGroup.get('routeId') as FormControl
    }

    public getCalendarNameFormControl(): FormControl {
        return this.getTimetableByName().get('calendarName') as FormControl
    }

    public buildCreateTimetableRequest() {
        const timetableGeneratorPayload: TimetableGeneratorPayload = {};

        const frontDepartures: FormGroup = this.getTimetableByName()?.get('front') as FormGroup;
        const backDepartures: FormGroup = this.getTimetableByName()?.get('back') as FormGroup;

        timetableGeneratorPayload.front = this.buildTimetablePayload(frontDepartures);
        timetableGeneratorPayload.back = this.buildTimetablePayload(backDepartures);
        timetableGeneratorPayload.calendarName = this.getCalendarNameFormControl().value;

        return timetableGeneratorPayload;
    }

    private buildTimetablePayload(frontDepartures: FormGroup): TimetablePayload {
        const timetablePayload: TimetablePayload = {};
        const departuresControls: FormArray<FormGroup> = frontDepartures?.get('departures') as FormArray<FormGroup>
        timetablePayload.departures = departuresControls.controls
            .filter((group: FormGroup): boolean => group.get('minutes').value != null)
            .map((group: FormGroup): TimetableStopTime => {
                const stopTime: TimetableStopTime = {}
                stopTime.time = moment()
                    .hours(group.get('hour').value)
                    .minutes(group.get('minutes').value)
                    .seconds(0)
                    .format('HH:mm');
                stopTime.designation = group.get('symbol').value;
                return stopTime;
            });
        return timetablePayload;
    }

    public saveGeneratedTimetable(): void {
        this.isSubmitted = true;

        const ee = getFormValidationErrors(this.formGroup);
        console.log(ee);
        console.log(this.formGroup.valid);
        if (this.formGroup.valid) {
            const payload: TimetableGeneratorPayload = this.buildCreateTimetableRequest();
            const request: CreateTimetableGeneratorRequest = {};
            request.timetables = payload;
            request.routeId = {};
            request.routeId = this.formGroup.get('routeId').value;

            console.log(request);

            this.timetableGeneratorService.createTimetableGenerator(this.agencyStorageService.getInstance(), request).subscribe(response => {
            });
        } else {
            this.scrollToFirstError();
        }
    }

    public checkControlHasError(controlName: string, errorName: string): boolean {
        return this.isSubmitted && this.getTimetableByName().get(controlName).errors?.[errorName] || false;
    }

    public scrollToFirstError(): void {
        setTimeout(() => {
            // const invalidControl = document.querySelector('.ng-invalid');
            const invalidControl = document.querySelector('.text-danger');
            invalidControl?.scrollIntoView({behavior: 'smooth', block: 'center'});
        });
    }

}
