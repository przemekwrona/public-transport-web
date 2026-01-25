import {
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
    styleUrl: './calendar-picker.component.scss'
    // changeDetection: ChangeDetectionStrategy.OnPush,
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
    @Output() public includeDaysChange: EventEmitter<Date> = new EventEmitter<Date>();

    @Input() public excludeDays: Date[] = [];
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
        if (changes['weekdays'] && changes['weekdays'].previousValue) {
            const start: moment.Moment = this.selectedMonth;
            const end: moment.Moment = start.clone().endOf('month');

            for (let day: moment.Moment = start.clone(); day.isSameOrBefore(end); day.add(1, 'day')) {
                const includedDay: number = this.includeDays.findIndex((date: Date): boolean => this.isSameDay(day.toDate(), date));
                if (includedDay > -1) {
                    const isWeekday: boolean = this.weekdays.includes(day.day());
                    if (isWeekday) {
                        this.includeDaysChange.emit(day.toDate());
                    }
                }
            }

            for (let day: moment.Moment = start.clone(); day.isSameOrBefore(end); day.add(1, 'day')) {
                const excludedDayIndex: number = this.excludeDays.findIndex((date: Date): boolean => this.isSameDay(day.toDate(), date));
                if (excludedDayIndex > -1) {
                    const isWeekday: boolean = this.weekdays.includes(day.day());
                    if(!isWeekday) {
                        console.log(isWeekday + '    ' + day.format('yyyy-MM-DD'));
                        this.excludeDaysChange.emit(day.toDate());
                    }
                }
            }

            this.calendar.updateTodaysDate();
        }
    }

    public selectedChange($event: Date) {
        this.handleDay($event);
        this.calendar.updateTodaysDate();
    }

    dateClass: (date: Date) => string | string[] = (date: Date): string | string[] => {
        if (this.weekdays.includes(date.getDay())) {
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
        if (this.weekdays.includes(clickedDate.getDay())) {
            const index: number = this.excludeDays.findIndex(d => this.isSameDay(d, clickedDate));
            if (index > -1) {
                this.excludeDaysChange.emit(clickedDate);
            } else {
                this.excludeDaysChange.emit(clickedDate);
            }
        } else {
            const index: number = this.includeDays.findIndex(d => this.isSameDay(d, clickedDate));
            if (index > -1) {
                this.includeDaysChange.emit(clickedDate);
            } else {
                this.includeDaysChange.emit(clickedDate);
            }
        }
    }

    private isSameDay(firstDate: Date, secondDate: Date): boolean {
        return firstDate.getFullYear() === secondDate.getFullYear()
            && firstDate.getMonth() === secondDate.getMonth()
            && firstDate.getDate() === secondDate.getDate();
    }

    public getCurrentMonth(): string {
        return `${this.selectedMonthDate.getMonth() + 1}`;
    }

}
