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
    GetCalendarsResponse, RouteId,
    TimetableGeneratorFilterByRoutesResponse,
    TimetableGeneratorPayload,
    TimetableGeneratorService,
    TimetablePayload,
    TimetableStopTime, TripFilter, TripResponse
} from "../../../generated/public-transport-api";
import {ActivatedRoute, Router} from "@angular/router";
import {NgxMaterialTimepickerModule} from "ngx-material-timepicker";
import {CommonModule} from "@angular/common";
import {NgxMatSelectSearchModule} from "ngx-mat-select-search";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {RouteNameNormPipe} from "./route-id-normalization.pipe";
import moment from "moment";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {AllValidationErrors, FormUtils} from "../../../shared/form.utils";
import {NotificationService} from "../../../shared/notification.service";

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
    public tripResponse: TripResponse = {};
    public isSubmitted: boolean = false;

    /** control for the MatSelect filter keyword */
    public bankFilterCtrl: FormControl<string> = new FormControl<string>('');

    get controlTimetables(): FormArray<FormGroup> {
        return this.formGroup.get('timetables') as FormArray<FormGroup>;
    }

    constructor(private formBuilder: FormBuilder, private _route: ActivatedRoute, private timetableGeneratorService: TimetableGeneratorService, private agencyStorageService: AgencyStorageService, private notificationService: NotificationService, private router: Router) {
    }

    ngOnInit(): void {
        this.formGroup = this.formBuilder.group({
            routeId: [null, [Validators.required]],
            timetables: this.formBuilder.array([this.buildTimetable()])
        });

        this.formGroup.get('routeId').valueChanges.subscribe((routeId: RouteId) => this.findTripByRouteId(routeId));

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
        timetablePayload.startDate = frontDepartures?.get('startTime').value;
        timetablePayload.endDate = frontDepartures?.get('endTime').value;
        timetablePayload.interval = frontDepartures?.get('interval').value;
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

        const errors: AllValidationErrors[] = FormUtils.getFormValidationErrors(this.formGroup);
        console.log(errors);
        if (this.formGroup.valid) {
            const payload: TimetableGeneratorPayload = this.buildCreateTimetableRequest();
            const request: CreateTimetableGeneratorRequest = {};
            request.timetables = payload;
            request.routeId = {};
            request.routeId = this.formGroup.get('routeId').value;

            this.timetableGeneratorService.createTimetableGenerator(this.agencyStorageService.getInstance(), request).subscribe(response => {
                this.notificationService.showSuccess('Udało się utworzyć szkic rozkładu jazdy.Teraz musisz wygenerować brygady, aby rozkład był aktywny.');
                this.router.navigate(['/agency/timetables']).then();
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

    public findTripByRouteId(routeId: RouteId): void {
        const tripFilter: TripFilter = {};
        tripFilter.routeId = routeId;

        this.timetableGeneratorService.findTrips(this.agencyStorageService.getInstance(), tripFilter).subscribe((tripResponse: TripResponse) => this.tripResponse = tripResponse);
    }

}
