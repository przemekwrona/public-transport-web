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

    public includeDays: Date[] = [];
    public excludeDays: Date[] = [];

    public days: number[] = [];
    public year: number = 2025;

    public presentedMonths: Date[] = [];
    public weekdays: number[] = [];

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
        this.modelForm.get('startDate').valueChanges.subscribe((value: Date) => this.onChangeStartDate(value))
        this.modelForm.get('endDate').valueChanges.subscribe((value: Date) => this.onChangeEndtDate(value))

        this.modelForm.get('monday').valueChanges.subscribe((selected: boolean) => this.onChangeDateOfWeek(selected, 1))
        this.modelForm.get('tuesday').valueChanges.subscribe((selected: boolean) => this.onChangeDateOfWeek(selected, 2))
        this.modelForm.get('wednesday').valueChanges.subscribe((selected: boolean) => this.onChangeDateOfWeek(selected, 3))
        this.modelForm.get('thursday').valueChanges.subscribe((selected: boolean) => this.onChangeDateOfWeek(selected, 4))
        this.modelForm.get('friday').valueChanges.subscribe((selected: boolean) => this.onChangeDateOfWeek(selected, 5))
        this.modelForm.get('saturday').valueChanges.subscribe((selected: boolean) => this.onChangeDateOfWeek(selected, 6))
        this.modelForm.get('sunday').valueChanges.subscribe((selected: boolean) => this.onChangeDateOfWeek(selected, 7))

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

            this.includeDays = calendar.excluded.map((date: string): Date => moment(date).toDate());
            this.excludeDays = calendar.excluded.map((date: string): Date => moment(date).toDate());
        });
    }

    public onChangeDesignation(changedDesignation: string): void {
        if (changedDesignation.length > 0) {
            this.modelForm.get('description').setValue(this.CALENDAR_SYMBOLS[changedDesignation]);
        }
    }

    public onChangeStartDate(startDate: Date): void {
        const endDate: Date = this.modelForm.get('endDate').value as Date;
        this.presentedMonths = this.getRangeCalendars(startDate, endDate);
    }

    public onChangeEndtDate(endDate: Date): void {
        const startDate: Date = this.modelForm.get('startDate').value as Date;
        this.presentedMonths = this.getRangeCalendars(startDate, endDate);
    }

    public onChangeDateOfWeek(selected: boolean, numberOfDay: number) {
        this.weekdays = selected
            ? [...new Set([...this.weekdays, numberOfDay])]
            : this.weekdays.filter(day => day !== numberOfDay);
    }

    private getRangeCalendars(startDate: Date, endDate: Date): Date[] {
        const start: moment.Moment = moment(startDate).startOf('month');
        const end: moment.Moment = moment(endDate).startOf('month');

        let results: Date[] = [];
        while (end.isSameOrAfter(start)) {
            results.push(start.clone().toDate());
            start.add(1, 'month');
        }

        return results;
    }

    public getYearsOfPresentedMonths(): number[] {
        return uniq(this.presentedMonths.map((presentedDate: Date): number => presentedDate.getFullYear()));
    }

    public getIncludeDays(): string[] {
        return this.includeDays.map((date: Date) => moment(date).format('yyyy-MM-dd'));
    }

    public getGroupedIncludedDaysByYear(year: number) {
        return this.includeDays.filter((date: Date) => date.getFullYear() === year).sort();
    }

    public getExcludeDays(): string[] {
        return this.excludeDays.map((date: Date) => moment(date).format('yyyy-MM-DD'));
    }

    public findExcludedDaysByYear(year: number): Date[] {
        return this.excludeDays.filter((date: Date) => date.getFullYear() === year).sort();
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
        payload.body.included = this.getIncludeDays();
        payload.body.excluded = this.getExcludeDays();

        this.calendarService.createCalendar(this.loginService.getInstance(), payload).subscribe(status => {
        });
    }


    public update(): void {
        const payload: CalendarPayload = {};
        payload.body = this.calendarBody;
        payload.body.included = this.getIncludeDays();
        payload.body.excluded = this.getExcludeDays();

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
