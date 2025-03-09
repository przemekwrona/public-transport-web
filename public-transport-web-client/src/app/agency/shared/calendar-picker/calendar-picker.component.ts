import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges
} from '@angular/core';
import moment from "moment";

@Component({
    selector: 'app-calendar-picker',
    templateUrl: './calendar-picker.component.html',
    styleUrl: './calendar-picker.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CalendarPickerComponent implements OnInit, OnChanges {

    @Input() public year: number;
    @Input() public month: number;

    @Input() public weekdays: number[] = []

    @Input() public includeDays: Set<string> = new Set<string>();
    @Input() public excludeDays: Set<string> = new Set<string>();

    @Output() public includeDaysChange: EventEmitter<Set<string>> = new EventEmitter<Set<string>>();
    @Output() public excludeDaysChange: EventEmitter<Set<string>> = new EventEmitter<Set<string>>();

    public selectedMonth: moment.Moment;

    public holidays: string[] = ['01-01', '01-06', '12-24',
        '04-20', '04-21',
        '05-01', '05-03',
        '06-19',
        '11-01', '11-11',
        '12-25', '12-26'];

    ngOnInit(): void {
        this.selectedMonth = moment(`${this.year}-${this.month}`, 'YYYY-MM');
    }

    public getCalendar(): moment.Moment[] {
        let currentDate: moment.Moment = moment(`${this.year}-${this.month}`, 'YYYY-MM');
        let end: moment.Moment = moment(`${this.year}-${this.month}`, 'YYYY-MM').add(currentDate.daysInMonth(), 'days');

        const dates: moment.Moment[] = []

        for (let i: number = 0; i < end.diff(currentDate, 'days'); i++) {
            dates.push(moment(`${this.year}-${this.month}`, 'YYYY-MM').add(i, 'days'));
        }

        return dates;
    }

    public isIncludedWeekday(day: moment.Moment): boolean {
        return this.weekdays.includes(day.weekday()) && !this.excludeDays.has(day.format('YYYY-MM-DD'));
    }

    public handleDay(day: moment.Moment): void {
        const value: string = day.format('YYYY-MM-DD');
        if (this.weekdays.includes(day.weekday())) {
            if (this.excludeDays.has(value)) {
                this.excludeDays.delete(value);
            } else {
                this.excludeDays.add(value);
            }
            this.excludeDaysChange.emit(this.excludeDays);
        } else {
            if (this.includeDays.has(value)) {
                this.includeDays.delete(value);
            } else {
                this.includeDays.add(value);
            }
            this.includeDaysChange.emit(this.includeDays);
        }
    }

    public isIncluded(day: moment.Moment): boolean {
        return this.includeDays.has(day.format('YYYY-MM-DD'));
    }

    public isExcluded(day: moment.Moment): boolean {
        return this.excludeDays.has(day.format('YYYY-MM-DD'));
    }

    public isHoliday(day: moment.Moment): boolean {
        return this.holidays.includes(day.format('MM-DD'));
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['weekdays'].currentValue) {
            for (let includedDay of this.includeDays.values()) {
                const day: moment.Moment = moment(includedDay);
                if (this.weekdays.includes(day.weekday())) {
                    this.includeDays.delete(includedDay);
                }
            }

            for (let excludeDay of this.excludeDays.values()) {
                const day: moment.Moment = moment(excludeDay);
                if (!this.weekdays.includes(day.weekday())) {
                    this.excludeDays.delete(excludeDay);
                }
            }
        }
    }

}
