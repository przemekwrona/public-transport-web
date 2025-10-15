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

@Component({
    selector: 'app-calendars-editor',
    templateUrl: './calendars-editor.component.html',
    styleUrl: './calendars-editor.component.scss'
})
export class CalendarsEditorComponent implements OnInit {

    private componentMode: CalendarEditorComponentMode;
    private queryCalendarName: string = '';

    public calendarBody: CalendarBody = {};

    public includeDays: Set<string> = new Set<string>();
    public excludeDays: Set<string> = new Set<string>();

    public days: number[] = [];
    public year: number = 2025;

    public CALENDAR_SYMBOLS: { [key: string]: string } = {
        D: 'kursuje od poniedziałku do piątku oprócz świąt',
        C: 'kursuje w soboty, niedziele i święta',
        6: 'kursuje w soboty',
        7: 'kursuje w niedziele'
    }

    constructor(private calendarService: CalendarService, private _route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this._route.data.subscribe(data => this.componentMode = data['mode']);
        this._route.queryParams.subscribe(params => this.queryCalendarName = params['name']);

        this.calendarBody.designation = '';
        this.calendarBody.description = '';
        this.calendarBody.startDate = '2025-03-10';
        this.calendarBody.endDate = '2025-03-10';

        this.calendarBody.monday = false;
        this.calendarBody.tuesday = false;
        this.calendarBody.wednesday = false;
        this.calendarBody.thursday = false;
        this.calendarBody.friday = false;
        this.calendarBody.saturday = false;
        this.calendarBody.sunday = false;

        this._route.data.subscribe(data => {
            const calendar: CalendarBody = data['calendar'];
            this.calendarBody.calendarName = calendar.calendarName;
            this.calendarBody.designation = calendar.designation;
            this.calendarBody.description = calendar.description;
            this.calendarBody.startDate = calendar.startDate;
            this.calendarBody.endDate = calendar.endDate;

            this.calendarBody.monday = calendar.monday;
            this.calendarBody.tuesday = calendar.tuesday;
            this.calendarBody.wednesday = calendar.wednesday;
            this.calendarBody.thursday = calendar.thursday;
            this.calendarBody.friday = calendar.friday;
            this.calendarBody.saturday = calendar.saturday;
            this.calendarBody.sunday = calendar.sunday;

            this.includeDays = new Set(calendar.included)
            this.excludeDays = new Set(calendar.excluded)
        });
    }

    public onChangeDesignation(changedDesignation: string): void {
        if (changedDesignation.length > 0) {
            this.calendarBody.description = this.CALENDAR_SYMBOLS[changedDesignation];
        }
    }

    public getDays(): number[] {
        const days: number[] = [];

        if (this.calendarBody.monday) {
            days.push(1);
        }
        if (this.calendarBody.tuesday) {
            days.push(2);
        }
        if (this.calendarBody.wednesday) {
            days.push(3);
        }
        if (this.calendarBody.thursday) {
            days.push(4);
        }
        if (this.calendarBody.friday) {
            days.push(5);
        }
        if (this.calendarBody.saturday) {
            days.push(6);
        }
        if (this.calendarBody.sunday) {
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

        this.calendarService.createCalendar(payload).subscribe(status => {
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

        this.calendarService.updateCalendar(request).subscribe(status => {
        });
    }

}
