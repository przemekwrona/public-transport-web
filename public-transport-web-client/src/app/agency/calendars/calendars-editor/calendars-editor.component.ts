import {Component} from '@angular/core';
import {groupBy, uniq} from "lodash";
import moment from "moment";
import {CalendarsService} from "../calendars.service";

@Component({
    selector: 'app-calendars-editor',
    templateUrl: './calendars-editor.component.html',
    styleUrl: './calendars-editor.component.scss'
})
export class CalendarsEditorComponent {

    public monday: boolean = false;
    public tuesday: boolean = false;
    public wednesday: boolean = false;
    public thursday: boolean = false;
    public friday: boolean = false;
    public saturday: boolean = false;
    public sunday: boolean = false;

    public includeDays: Set<string> = new Set<string>();
    public excludeDays: Set<string> = new Set<string>();

    public days: number[] = [];

    public year: number = 2025;

    public dateFrom: string = '2025-03-10';
    public dateTo: string = '2025-03-10';

    constructor(private calendarsService: CalendarsService) {
    }

    public getDays(): number[] {
        const days: number[] = [];

        if (this.monday) {
            days.push(1);
        }
        if (this.tuesday) {
            days.push(2);
        }
        if (this.wednesday) {
            days.push(3);
        }
        if (this.thursday) {
            days.push(4);
        }
        if (this.friday) {
            days.push(5);
        }
        if (this.saturday) {
            days.push(6);
        }
        if (this.sunday) {
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

    }

}
