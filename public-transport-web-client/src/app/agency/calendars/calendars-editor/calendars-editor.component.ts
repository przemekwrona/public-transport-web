import {Component, OnInit} from '@angular/core';
import {groupBy, uniq} from "lodash";
import moment from "moment";
import {CalendarsService} from "../calendars.service";
import {CalendarBody, CalendarPayload} from "../../../generated/public-transport";

@Component({
    selector: 'app-calendars-editor',
    templateUrl: './calendars-editor.component.html',
    styleUrl: './calendars-editor.component.scss'
})
export class CalendarsEditorComponent implements OnInit {

    public calendarBody: CalendarBody = {};

    public includeDays: Set<string> = new Set<string>();
    public excludeDays: Set<string> = new Set<string>();

    public days: number[] = [];
    public year: number = 2025;

    constructor(private calendarsService: CalendarsService) {
    }

    ngOnInit(): void {
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

    public save(): void {
        const payload: CalendarPayload = {};
        payload.body = this.calendarBody;
        payload.body.included = [...this.includeDays];
        payload.body.excluded = [...this.excludeDays];

        this.calendarsService.createCalendar(payload)
    }

}
