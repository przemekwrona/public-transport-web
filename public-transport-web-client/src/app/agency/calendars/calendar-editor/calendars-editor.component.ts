import {Component, OnInit} from '@angular/core';
import {uniq, isEmpty} from "lodash";
import moment from "moment";
import {
    CalendarBody,
    CalendarPayload,
    CalendarService,
    UpdateCalendarRequest
} from "../../../generated/public-transport-api";
import {ActivatedRoute, Router} from "@angular/router";
import {CalendarEditorComponentMode} from "./calendar-editor-component-mode";
import {LoginService} from "../../../auth/login.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {faCalendar} from "@fortawesome/free-solid-svg-icons";
import {NotificationService} from "../../../shared/notification.service";

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

    public includeDaysMap: Map<string, Date[]> = new Map<string, Date[]>;
    public excludeDaysMap: Map<string, Date[]> = new Map<string, Date[]>;

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

    constructor(private calendarService: CalendarService, private loginService: LoginService, private _route: ActivatedRoute, private router: Router, private formBuilder: FormBuilder, private notificationService: NotificationService) {
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

            calendar.included.forEach((includedDate: string) => {
                const included: moment.Moment = moment(includedDate);
                const dateKey: string = moment(includedDate).startOf('month').format('yyyy-MM-DD');
                const isEmptyDateKey: boolean = isEmpty(this.includeDaysMap.get(dateKey));
                if (isEmptyDateKey) {
                    this.includeDaysMap.set(dateKey, [included.toDate()])
                } else {
                    this.includeDaysMap.get(dateKey).push(included.toDate());
                }

            });

            calendar.excluded.forEach((excludedDate: string) => {
                const excluded: moment.Moment = moment(excludedDate);
                const dateKey: string = moment(excludedDate).startOf('month').format('yyyy-MM-DD');
                const isEmptyDateKey: boolean = isEmpty(this.excludeDaysMap.get(dateKey));
                if (isEmptyDateKey) {
                    this.excludeDaysMap.set(dateKey, [excluded.toDate()])
                } else {
                    this.excludeDaysMap.get(dateKey).push(excluded.toDate());
                }
            });
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
        this.refreshIncludedAndExcludedDays();
    }

    public onChangeEndtDate(endDate: Date): void {
        const startDate: Date = this.modelForm.get('startDate').value as Date;
        this.presentedMonths = this.getRangeCalendars(startDate, endDate);
        this.refreshIncludedAndExcludedDays();
    }

    private refreshIncludedAndExcludedDays() {
        this.presentedMonths
            .map((value: Date): string => moment(value).format('yyyy-MM-DD'))
            .forEach((keyDate: string): void => {
                this.includeDaysMap.set(keyDate, isEmpty(this.includeDaysMap.get(keyDate)) ? [] : this.includeDaysMap.get(keyDate));
                this.excludeDaysMap.set(keyDate, isEmpty(this.excludeDaysMap.get(keyDate)) ? [] : this.excludeDaysMap.get(keyDate));
            });
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
        return this.presentedMonths
            .map((date: Date): string => moment(date).format('yyyy-MM-DD'))
            .map((includedDateKey: string): Date[] => this.includeDaysMap.get(includedDateKey))
            .flatMap((included: Date[]): Date[] => included)
            .map((date: Date): string => moment(date).format('yyyy-MM-DD'));
    }

    public getExcludeDays(): string[] {
        return this.presentedMonths
            .map((date: Date): string => moment(date).format('yyyy-MM-DD'))
            .map((excludedDateKey: string): Date[] => this.excludeDaysMap.get(excludedDateKey))
            .flatMap((excluded: Date[]): Date[] => excluded)
            .map((date: Date): string => moment(date).format('yyyy-MM-DD'));
    }

    public isCreate(): boolean {
        return this.componentMode === CalendarEditorComponentMode.CREATE;
    }

    public isEdit(): boolean {
        return this.componentMode === CalendarEditorComponentMode.EDIT;
    }

    public save(): void {
        const payload: CalendarPayload = {};
        // payload.body = this.calendarBody;
        payload.body.included = this.getIncludeDays();
        payload.body.excluded = this.getExcludeDays();

        this.calendarService.createCalendar(this.loginService.getInstance(), payload).subscribe(status => {
            this.notificationService.showSuccess('Kalendarz został utworzony');
            this.notificationService.showInfo('Zaraz zostaniesz przekieowany na listę kalendarzy');
            setTimeout(() => {
                this.navigateToCalendars();
            }, 3000);
        });
    }

    public update(): void {
        const body: CalendarBody = {}
        body.calendarName = this.queryCalendarName;
        body.designation = this.modelForm.get('designation').value;
        body.description = this.modelForm.get('description').value;
        body.startDate = moment(this.modelForm.get('startDate').value).format('yyyy-MM-DD');
        body.endDate = moment(this.modelForm.get('endDate').value).format('yyyy-MM-DD');
        body.monday = this.modelForm.get('monday').value;
        body.tuesday = this.modelForm.get('tuesday').value;
        body.wednesday = this.modelForm.get('wednesday').value;
        body.thursday = this.modelForm.get('thursday').value;
        body.friday = this.modelForm.get('friday').value;
        body.saturday = this.modelForm.get('saturday').value;
        body.sunday = this.modelForm.get('sunday').value;
        body.included = this.getIncludeDays();
        body.excluded = this.getExcludeDays();

        const request: UpdateCalendarRequest = {};
        request.calendarName = this.queryCalendarName;
        request.body = body;

        this.calendarService.updateCalendar(this.loginService.getInstance(), request).subscribe(status => {
            this.notificationService.showSuccess('Kalendarz został zaktualizowany');
            this.notificationService.showInfo('Zaraz zostaniesz przekieowany na listę kalendarzy');
            setTimeout(() => {
                this.navigateToCalendars();
            }, 4000);
        });
    }

    public navigateToCalendars() {
        this.router.navigate(['/agency/calendars']).then(() => {
        });
    }

    public hasError(controlName: string, error: string): boolean {
        return this.modelForm.get(controlName).hasError(error);
    }

    public control(controlName: string): FormControl {
        return this.modelForm.get(controlName) as FormControl;
    }

    public buildKey(year: number, month: number): string {
        if (month < 10) {
            return `${year}-0${month}-01`;
        }
        return `${year}-${month}-01`;
    }

    public handleOnChangeIncludeDate(date: Date): void {
        const dateKey: string = moment(date).startOf('month').format('yyyy-MM-DD');
        if (isEmpty(this.includeDaysMap.get(dateKey))) {
            this.includeDaysMap.set(dateKey, [date]);
        } else {
            const includedDates: Date[] = this.includeDaysMap.get(dateKey);
            const dateIndex: number = includedDates.findIndex((includedDate: Date) => this.sameDay(includedDate, date));

            if (dateIndex > -1) {
                includedDates.splice(dateIndex, 1);
            } else {
                includedDates.push(date);
            }
        }
    }

    public getIncludedDates(dateKey: string): Date[] {
        return (this.includeDaysMap.get(dateKey) || []).sort((a, b) => moment(a).unix() - moment(b).unix())
    }

    public getExcludedDates(dateKey: string): Date[] {
        return (this.excludeDaysMap.get(dateKey) || []).sort((a, b) => moment(a).unix() - moment(b).unix())
    }

    public handleOnChangeExcludeDate(date: Date): void {
        const dateKey: string = moment(date).startOf('month').format('yyyy-MM-DD');
        if (isEmpty(this.excludeDaysMap.get(dateKey))) {
            this.excludeDaysMap.set(dateKey, [date]);
        } else {
            const excludedDates: Date[] = this.excludeDaysMap.get(dateKey);
            const dateIndex: number = excludedDates.findIndex((excludedDate: Date): boolean => this.sameDay(excludedDate, date));

            if (dateIndex > -1) {
                excludedDates.splice(dateIndex, 1);
            } else {
                excludedDates.push(date);
            }
        }
    }

    private sameDay(firstDate: Date, secondDate: Date): boolean {
        return firstDate.getFullYear() === secondDate.getFullYear()
            && firstDate.getMonth() === secondDate.getMonth()
            && firstDate.getDate() === secondDate.getDate();
    }

}
