import {Component} from '@angular/core';
import moment from "moment";

@Component({
    selector: 'app-calendar-input',
    templateUrl: './calendar-input.component.html',
    styleUrl: './calendar-input.component.scss'
})
export class CalendarInputComponent {

    public isFocus: boolean = false;

    public selectedDate: string = moment().format('YYYY-MM-DD');

    public includeDays: Set<string> = new Set<string>;

    public onChangeSelectedDate(): void {
        if(moment(this.selectedDate, 'YYYY-MM-DD', true).isValid()) {
            const currentIncludeDays = new Set<string>();
            currentIncludeDays.add(this.selectedDate);
            this.includeDays = currentIncludeDays;
        }

    }

    public onChangeIncludeDays($event: Set<string>) {
        this.includeDays = $event;
        this.selectedDate = [...$event][0];
    }

}
