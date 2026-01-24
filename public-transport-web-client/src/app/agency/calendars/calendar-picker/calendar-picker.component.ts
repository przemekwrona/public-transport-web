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
import {MatCalendar, MatDatepickerModule} from "@angular/material/datepicker";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";

@Component({
    imports: [
        MatFormFieldModule,
        MatInputModule,
        MatDatepickerModule
    ],
    selector: 'app-calendar-picker',
    templateUrl: './calendar-picker.component.html',
    styleUrl: './calendar-picker.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CalendarPickerComponent implements OnInit, OnChanges {
    @ViewChild(MatCalendar) calendar!: MatCalendar<Date>;

    @Input() public year: number;
    @Input() public month: number;
    @Input() public singleSelection: boolean = false;
    @Input() public startDate: Date;
    @Input() public endDate: Date;

    @Input() public weekdays: number[] = []

    @Input() public includeDays: Date[] = [];
    @Output() public includeDaysChange: EventEmitter<Date[]> = new EventEmitter<Date[]>();

    @Input() public excludeDays: Date[] = [];
    @Output() public excludeDaysChange: EventEmitter<Date[]> = new EventEmitter<Date[]>();

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

    public handleDay(day: Date): void {

        if (this.weekdays.includes(day.getDay())) {
            const index: number = this.excludeDays.findIndex(d => this.sameDay(d, day));
            if (index > -1) {
                this.excludeDays.splice(index, 1); // remove
            } else {
                this.excludeDays.push(day); // add
            }
            this.excludeDaysChange.emit(this.excludeDays);
        } else {
            const index: number = this.includeDays.findIndex(d => this.sameDay(d, day));
            if (index > -1) {
                this.includeDays.splice(index, 1);
            } else {
                this.includeDays.push(day);
            }
            this.includeDaysChange.emit(this.excludeDays);
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (!changes['weekdays'].firstChange) {
            this.calendar.updateTodaysDate();
        }
    }

    public selectedChange($event: Date) {
        this.handleDay($event);
        this.calendar.updateTodaysDate();
    }

    dateClass: (date: Date) => string | string[] = (date: Date): string | string[] => {
        if (this.weekdays.includes(date.getDay())) {
            const index: number = this.excludeDays.findIndex(d => this.sameDay(d, date));
            if (index > -1) {
                return ['has-week-day', 'has-excluded-day'];
            } else {
                return 'has-week-day';
            }
        } else {
            const index: number = this.includeDays.findIndex(d => this.sameDay(d, date));
            if (index > -1) {
                return ['has-no-week-day', 'has-included-day']
            } else {
                return 'has-no-week-day';
            }
        }
    };

    private sameDay(firstDate: Date, secondDate: Date): boolean {
        return firstDate.getFullYear() === secondDate.getFullYear()
            && firstDate.getMonth() === secondDate.getMonth()
            && firstDate.getDate() === secondDate.getDate();
    }

}
