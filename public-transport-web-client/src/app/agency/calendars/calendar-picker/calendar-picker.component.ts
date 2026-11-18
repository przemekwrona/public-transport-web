import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges, ViewChild,
  ChangeDetectionStrategy
} from '@angular/core';
import moment from "moment";
import {MatCalendar, MatDatepickerModule} from "@angular/material/datepicker";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {TranslocoModule} from "@jsverse/transloco";

@Component({
    imports: [
        MatFormFieldModule,
        MatInputModule,
        MatDatepickerModule,
        TranslocoModule
    ],
    selector: 'app-calendar-picker',
    templateUrl: './calendar-picker.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './calendar-picker.component.scss'
})
export class CalendarPickerComponent implements OnInit, OnChanges {
    @ViewChild(MatCalendar) calendar!: MatCalendar<Date>;

    @Input() public year: number;
    @Input() public month: number;
    @Input() public singleSelection: boolean = false;
    @Input() public startDate: Date;
    @Input() public endDate: Date;

    @Input() public weekdays: number[] = []

    private includedDates: Date[] = [];
    private excludedDates: Date[] = [];

    @Input()
    public set includeDays(value: Date[] | null | undefined) {
        this.setDates(this.includedDates, value, (next: Date[]) => this.includedDates = next);
    }

    public get includeDays(): Date[] {
        return this.includedDates;
    }

    @Output() public includeDaysChange: EventEmitter<Date> = new EventEmitter<Date>();

    @Input()
    public set excludeDays(value: Date[] | null | undefined) {
        this.setDates(this.excludedDates, value, (next: Date[]) => this.excludedDates = next);
    }

    public get excludeDays(): Date[] {
        return this.excludedDates;
    }

    @Output() public excludeDaysChange: EventEmitter<Date> = new EventEmitter<Date>();

    public selectedMonth: moment.Moment;
    public selectedMonthDate!: Date;

    ngOnInit(): void {
        if (this.year == null) {
            this.year = moment().year();
        }
        if (this.month == null) {
            this.month = moment().month();
        }
        this.selectedMonth = moment(`${this.year}-${this.month}-01`, 'YYYY-MM-DD');
        this.selectedMonthDate = this.selectedMonth.toDate();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['weekdays'] && changes['weekdays'].previousValue && this.selectedMonth) {
            const start: moment.Moment = this.selectedMonth;
            const end: moment.Moment = start.clone().endOf('month');

            for (let day: moment.Moment = start.clone(); day.isSameOrBefore(end); day.add(1, 'day')) {
                const current: Date = day.toDate();
                const includedDay: number = this.includeDays.findIndex((date: Date): boolean => this.isSameDay(current, date));
                if (includedDay > -1) {
                    const isWeekday: boolean = this.isConfiguredWeekday(day.day());
                    if (isWeekday) {
                        this.includedDates = this.toggleDate(this.includedDates, current);
                        this.includeDaysChange.emit(current);
                    }
                }
            }

            for (let day: moment.Moment = start.clone(); day.isSameOrBefore(end); day.add(1, 'day')) {
                const current: Date = day.toDate();
                const excludedDayIndex: number = this.excludeDays.findIndex((date: Date): boolean => this.isSameDay(current, date));
                if (excludedDayIndex > -1) {
                    const isWeekday: boolean = this.isConfiguredWeekday(day.day());
                    if (!isWeekday) {
                        this.excludedDates = this.toggleDate(this.excludedDates, current);
                        this.excludeDaysChange.emit(current);
                    }
                }
            }

            this.refreshCalendar();
        }

        if (changes['startDate'] && changes['startDate'].previousValue) {
            this.refreshCalendar();
        }

        if (changes['endDate'] && changes['endDate'].previousValue) {
            this.refreshCalendar();
        }
    }

    public selectedChange($event: Date) {
        this.handleDay($event);
        this.refreshCalendar();
    }

    dateClass: (date: Date) => string | string[] = (date: Date): string | string[] => {
        const momentDate: moment.Moment = moment(date);
        const startMomentDate: moment.Moment = moment(this.startDate);
        const endMomentDate: moment.Moment = moment(this.endDate);
        if (momentDate.isBefore(startMomentDate) || momentDate.isAfter(endMomentDate)) {
            return '';
        }

        if (this.isConfiguredWeekday(date.getDay())) {
            const index: number = this.excludeDays.findIndex((excludedDate: Date): boolean => this.isSameDay(excludedDate, date));
            if (index > -1) {
                return ['has-week-day', 'has-excluded-day'];
            } else {
                return 'has-week-day';
            }
        } else {
            const index: number = this.includeDays.findIndex((includedDate: Date): boolean => this.isSameDay(includedDate, date));
            if (index > -1) {
                return ['has-no-week-day', 'has-included-day']
            } else {
                return 'has-no-week-day';
            }
        }
    };

    private handleDay(clickedDate: Date): void {
        if (this.isConfiguredWeekday(clickedDate.getDay())) {
            this.excludedDates = this.toggleDate(this.excludedDates, clickedDate);
            this.excludeDaysChange.emit(clickedDate);
        } else {
            this.includedDates = this.toggleDate(this.includedDates, clickedDate);
            this.includeDaysChange.emit(clickedDate);
        }
    }

    private isConfiguredWeekday(jsDay: number): boolean {
        const weekdays: number[] = this.weekdays ?? [];
        if (weekdays.includes(jsDay)) {
            return true;
        }
        return jsDay === 0 && weekdays.includes(7);
    }

    private toggleDate(dates: Date[], clickedDate: Date): Date[] {
        const index: number = dates.findIndex((date: Date): boolean => this.isSameDay(date, clickedDate));
        if (index > -1) {
            return dates.filter((_: Date, dateIndex: number): boolean => dateIndex !== index);
        }
        return [...dates, clickedDate];
    }

    private setDates(current: Date[], value: Date[] | null | undefined, assign: (next: Date[]) => void): void {
        const next: Date[] = value ?? [];
        if (next === current) {
            return;
        }
        assign(next);
        this.refreshCalendar();
    }

    private refreshCalendar(): void {
        this.calendar?.updateTodaysDate();
    }

    private isSameDay(firstDate: Date, secondDate: Date): boolean {
        return firstDate.getFullYear() === secondDate.getFullYear()
            && firstDate.getMonth() === secondDate.getMonth()
            && firstDate.getDate() === secondDate.getDate();
    }

    public getCurrentMonth(): number {
        return this.selectedMonthDate.getMonth() + 1;
    }

    public isInRange(currentDate: Date): boolean {
        const currentMoment: moment.Moment = moment(currentDate);
        const startMoment: moment.Moment = moment(this.startDate).startOf('month');
        const endMoment: moment.Moment = moment(this.endDate).endOf('month');

        return currentMoment.isSameOrAfter(startMoment) && currentMoment.isSameOrBefore(endMoment);
    }

}
