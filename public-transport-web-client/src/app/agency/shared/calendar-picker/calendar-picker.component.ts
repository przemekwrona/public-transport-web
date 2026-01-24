import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges, ViewChild
} from '@angular/core';
import moment from "moment";
import {MatCalendar} from "@angular/material/datepicker";

class DateManager {

    private included: Date[];
    private excluded: Date[];

    constructor() {
    }

    isIncluded(date: Date): boolean {
        return this.included.includes(date);
    }

    isExcluded(date: Date): boolean {
        return this.excluded.includes(date);
    }

    handelDate(date: Date) {
        if (this.isIncluded(date)) {
            const index: number = this.included.indexOf(date);
            if (index > -1) {
                this.included.splice(index, 1);
            }
        }
    }
}


@Component({
    selector: 'app-calendar-picker',
    templateUrl: './calendar-picker.component.html',
    styleUrl: './calendar-picker.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class CalendarPickerComponent implements OnInit, OnChanges {
    @ViewChild(MatCalendar) calendar!: MatCalendar<Date>;

    @Input() showNavigation: boolean = false;
    @Input() public year: number;
    @Input() public month: number;
    @Input() public singleSelection: boolean = false;
    @Input() public startDate: Date;
    @Input() public endDate: Date;

    @Input() public weekdays: number[] = []

    @Input() public includeDays: Set<string> = new Set<string>();
    @Input() public excludeDays: Set<string> = new Set<string>();

    @Output() public includeDaysChange: EventEmitter<Set<string>> = new EventEmitter<Set<string>>();
    @Output() public excludeDaysChange: EventEmitter<Set<string>> = new EventEmitter<Set<string>>();

    public selectedMonth: moment.Moment;
    public selectedMonthDate!: Date;

    private dateManager: DateManager = new DateManager();

    public holidays: string[] = ['01-01', '01-06', '12-24',
        '04-20', '04-21',
        '05-01', '05-03',
        '06-19',
        '11-01', '11-11',
        '12-25', '12-26'];

    ngOnInit(): void {
        if (this.year == null) {
            this.year = moment().year();
        }
        if (this.month == null) {
            this.month = moment().month();
        }
        this.selectedMonth = moment(`${this.year}-${this.month}-01`, 'YYYY-MM-DD');
        this.selectedMonthDate = this.selectedMonth.toDate();

        console.log(this.includeDays);
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
            if (this.singleSelection) {
                this.includeDays.clear();
                this.includeDays.add(value);
            } else {
                if (this.includeDays.has(value)) {
                    this.includeDays.delete(value);
                } else {
                    this.includeDays.add(value);
                }
            }

            this.includeDaysChange.emit(this.includeDays);
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['weekdays']?.currentValue) {
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

    public selectedChange($event: Date) {
        this.handleDay(moment($event));
        this.calendar.updateTodaysDate();
    }

    dateClass: (date: Date) => string | string[] = (date: Date): string | string[] => {
        const formattedDay = moment(date).format('yyyy-MM-DD');
        if (this.weekdays.includes(date.getDay())) {
            if (this.excludeDays.has(formattedDay)) {
                return ['has-week-day', 'has-excluded-day'];
            } else {
                return 'has-week-day';
            }
        } else {
            if (this.includeDays.has(formattedDay)) {
                return ['has-no-week-day', 'has-included-day']
            } else {
                return 'has-no-week-day';
            }
        }
    };

}
