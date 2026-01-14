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
    CalendarBody,
    CreateTimetableGeneratorRequest,
    GetCalendarsResponse, GetTimetableGeneratorDetailsResponse, RouteId,
    TimetableGeneratorFilterByRoutesResponse,
    TimetableGeneratorPayload,
    TimetableGeneratorService,
    TimetablePayload,
    TimetableStopTime, TripFilter, TripResponse
} from "../../../generated/public-transport-api";
import {ActivatedRoute, Data, Router, RouterModule} from "@angular/router";
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

export enum TimetableEditorComponentMode {
    CREATE, EDIT
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
        RouteNameNormPipe,
        RouterModule
    ],
    templateUrl: './timetable-editor.component.html',
    styleUrl: './timetable-editor.component.scss'
})
export class TimetableEditorComponent implements OnInit {

    public formGroup: FormGroup;
    public calendarsResponse: GetCalendarsResponse = {};
    public routes: TimetableGeneratorFilterByRoutesResponse = {};
    public tripResponse: TripResponse = {front: {}, back: {}};
    public isSubmitted: boolean = false;
    public timetableEditorComponentMode: TimetableEditorComponentMode;
    public timetableGeneratorDetailsResponse: GetTimetableGeneratorDetailsResponse | null;

    /** control for the MatSelect filter keyword */
    public bankFilterCtrl: FormControl<string> = new FormControl<string>('');

    get controlTimetables(): FormArray<FormGroup> {
        return this.formGroup.get('timetables') as FormArray<FormGroup>;
    }

    constructor(private formBuilder: FormBuilder, private route: ActivatedRoute, private router: Router, private timetableGeneratorService: TimetableGeneratorService, private agencyStorageService: AgencyStorageService, private notificationService: NotificationService) {
        this.formGroup = this.formBuilder.group({
            routeId: [null, [Validators.required]],
            timetables: this.formBuilder.array([this.buildTimetable()])
        });
        this.formGroup.get('routeId').valueChanges.subscribe((routeId: RouteId) => this.findTripByRouteId(routeId));
    }

    ngOnInit(): void {
        this.route.data.subscribe((data: Data) => this.timetableEditorComponentMode = data['mode']);
        this.route.data.subscribe(data => this.calendarsResponse = data['calendars']);
        this.route.data.subscribe(data => this.routes = data['routes']);
        this.route.data.subscribe(data => {
            this.timetableGeneratorDetailsResponse = data['timetableGenerator'] as GetTimetableGeneratorDetailsResponse;
            this.getRouteIdFormControl().setValue(this.timetableGeneratorDetailsResponse.timetableGeneratorId.routeId);
            this.getCalendarNameFormControl().setValue(this.timetableGeneratorDetailsResponse.timetables.calendarName);
        });

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

    public isModeCreator(): boolean {
        return this.timetableEditorComponentMode === TimetableEditorComponentMode.CREATE;
    }

    public isModeEditor(): boolean {
        return this.timetableEditorComponentMode === TimetableEditorComponentMode.EDIT;
    }

    public findCalendarByName(calendarName: string): CalendarBody  {
        return this.calendarsResponse.calendars.filter(calendar => calendar.calendarName === calendarName)[0];
    }

    public findTripByRouteId(routeId: RouteId): void {
        const agency: string = this.agencyStorageService.getInstance();
        const tripFilter: TripFilter = {} as TripFilter;
        tripFilter.routeId = routeId;
        this.timetableGeneratorService.findTrips(agency, tripFilter).subscribe((response: TripResponse) => {
            this.tripResponse = response;
        });
    }

    compareByRouteId = (a: RouteId, b: RouteId): boolean => a && b ? a.line === b.line && a.name === b.name && a.version === b.version : a === b;

}
