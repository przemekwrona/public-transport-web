import {Component, OnInit} from '@angular/core';
import {groupBy, uniq} from "lodash";
import moment from "moment";
import {
    CalendarBody,
    CalendarPayload,
    CalendarService,
    UpdateCalendarRequest
} from "../../../generated/public-transport-api";
import {ActivatedRoute} from "@angular/router";
import {CalendarEditorComponentMode} from "./calendar-editor-component-mode";
import {LoginService} from "../../../auth/login.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {faCalendar} from "@fortawesome/free-solid-svg-icons";

@Component({
    selector: 'app-calendars-editor',
    templateUrl: './calendars-editor.component.html',
    styleUrl: './calendars-editor.component.scss',
    standalone: false
})
export class CalendarsEditorComponent implements OnInit {

    protected readonly faCalendar = faCalendar;

    private componentMode: CalendarEditorComponentMode;
    private queryCalendarName: string = '';

    public modelForm: FormGroup;

    public calendarBody: CalendarBody = {};

    public includeDays: Set<string> = new Set<string>();
    public excludeDays: Set<string> = new Set<string>();

    public days: number[] = [];
    public year: number = 2025;

    public CALENDAR_SYMBOLS: { [key: string]: string } = {
        A: 'kursuje od poniedziałku do piątku',
        B: 'kursuje od poniedziałku do piątku oraz w niedzielę',
        C: 'kursuje w soboty, niedziele i święta',
        D: 'kursuje od poniedziałku do piątku oprócz świąt',
        E: 'kursuje od poniedziałku do soboty oprócz świąt',
        H: 'kursuje codziennie w okresie ferii letnich i zimowych oraz szkolnych przerw świątecznych',
        L: 'kursuje w okresie ferii letnich',
        S: 'kursuje w dni nauki szkolnej',
        1: 'kursuje w poniedziałki',
        2: 'kursuje w wtorki',
        3: 'kursuje w środy',
        4: 'kursuje w czwartki',
        5: 'kursuje w piątki',
        6: 'kursuje w soboty',
        7: 'kursuje w niedziele',
        a: 'nie kursuje w pierwszy dzień Świąt Wielkanocnych oraz w dniu 25.XII',
        b: 'nie kursuje w dniu 1.I, w pierwszy dzień Świąt Wielkanocnych i w dniu 25.XII',
        c: 'nie kursuje w dniu 1.I, w pierwszy dzień Świąt Wielkanocnych oraz w dniach 25 i 26.XII',
        d: 'nie kursuje w dniu 1.I, w pierwszy i drugi dzień Świąt Wielkanocnych oraz w dniach 25 i 26.XII',
        e: 'nie kursuje w okresie ferii letnich',
        f: 'nie kursuje w okresie ferii letnich i zimowych oraz szkolnych przerw świątecznych',
        g: 'nie kursuje w dniu 24.XII',
        h: 'nie kursuje w Wielką Sobotę oraz w dniu 24.XII',
        i: 'nie kursuje w dniu 26.XII',
        j: 'nie kursuje w dniu 27.XII',
        k: 'nie kursuje w drugi dzień Świąt Wielkanocnych oraz w dniu 26.XII',
        l: 'nie kursuje w dniu 31.XII',
        m: 'nie kursuje w dniach 24 i 31.XII',
        n: 'nie kursuje w Wielką Sobotę oraz w dniach 24 i 31.XII'
    }

    constructor(private calendarService: CalendarService, private loginService: LoginService, private _route: ActivatedRoute, private formBuilder: FormBuilder) {
    }

    ngOnInit(): void {
        this._route.data.subscribe(data => this.componentMode = data['mode']);
        this._route.queryParams.subscribe(params => this.queryCalendarName = params['name']);

        this.modelForm = this.formBuilder.group({
            designation: ['', [Validators.required]],
            description: ['', [Validators.required]],
            startDate: [moment().startOf('day').toDate(), [Validators.required]],
            endDate: [moment().endOf('year').toDate(), [Validators.required]],
            monday: [false],
            tuesday: [false],
            wednesday: [false],
            thursday: [false],
            friday: [false],
            saturday: [false],
            sunday: [false]
        })

        this.modelForm.get('designation').valueChanges.subscribe((value: string) => this.onChangeDesignation(value))

        this._route.data.subscribe(data => {
            const calendar: CalendarBody = data['calendar'];
            this.modelForm.get('designation').setValue(calendar.designation);
            this.modelForm.get('description').setValue(calendar.description);
            this.modelForm.get('startDate').setValue(calendar.startDate);
            this.modelForm.get('endDate').setValue(calendar.endDate);

            this.modelForm.get('monday').setValue(calendar.monday);
            this.modelForm.get('tuesday').setValue(calendar.tuesday);
            this.modelForm.get('wednesday').setValue(calendar.wednesday);
            this.modelForm.get('thursday').setValue(calendar.thursday);
            this.modelForm.get('friday').setValue(calendar.friday);
            this.modelForm.get('saturday').setValue(calendar.saturday);
            this.modelForm.get('sunday').setValue(calendar.sunday);

            this.includeDays = new Set(calendar.included)
            this.excludeDays = new Set(calendar.excluded)
        });
    }

    public onChangeDesignation(changedDesignation: string): void {
        if (changedDesignation.length > 0) {
            this.modelForm.get('description').setValue(this.CALENDAR_SYMBOLS[changedDesignation]);
        }
    }

    public getDays(): number[] {
        const days: number[] = [];

        if (this.modelForm.get('monday').value) {
            days.push(1);
        }
        if (this.modelForm.get('tuesday').value) {
            days.push(2);
        }
        if (this.modelForm.get('wednesday').value) {
            days.push(3);
        }
        if (this.modelForm.get('thursday').value) {
            days.push(4);
        }
        if (this.modelForm.get('friday').value) {
            days.push(5);
        }
        if (this.modelForm.get('saturday').value) {
            days.push(6);
        }
        if (this.modelForm.get('sunday').value) {
            days.push(0);
        }
        return days;
    }

    public getIncludeDays(): string[] {
        return [...this.includeDays];
    }

    public getUniqueIncludedYears(): number[] {
        return uniq(this.getIncludeDays().map((date: string): number => moment(date).year())).sort();
    }

    public getGroupedIncludedDaysByYear() {
        return groupBy(this.getIncludeDays(), (date: string) => moment(date).year());
    }

    public getExcludeDays(): string[] {
        return [...this.excludeDays];
    }

    public getUniqueExcludedYears(): number[] {
        return uniq(this.getExcludeDays().map((date: string): number => moment(date).year())).sort();
    }

    public getGroupedExcludedDaysByYear() {
        return groupBy(this.getExcludeDays(), (date: string) => moment(date).year());
    }

    public nextYear(): void {
        this.year++;
    }

    public prevYear(): void {
        this.year--;
    }

    public isCreate(): boolean {
        return this.componentMode === CalendarEditorComponentMode.CREATE;
    }

    public isEdit(): boolean {
        return this.componentMode === CalendarEditorComponentMode.EDIT;
    }

    public save(): void {
        const payload: CalendarPayload = {};
        payload.body = this.calendarBody;
        payload.body.included = [...this.includeDays];
        payload.body.excluded = [...this.excludeDays];

        this.calendarService.createCalendar(this.loginService.getInstance(), payload).subscribe(status => {
        });
    }


    public update(): void {
        const payload: CalendarPayload = {};
        payload.body = this.calendarBody;
        payload.body.included = [...this.includeDays];
        payload.body.excluded = [...this.excludeDays];

        const request: UpdateCalendarRequest = {};
        request.body = this.calendarBody;
        request.calendarName = this.queryCalendarName;

        this.calendarService.updateCalendar(this.loginService.getInstance(), request).subscribe(status => {
        });
    }

    public hasError(controlName: string, error: string): boolean {
        return this.modelForm.get(controlName).hasError(error);
    }

    public control(controlName: string): FormControl {
        return this.modelForm.get(controlName) as FormControl;
    }

}
