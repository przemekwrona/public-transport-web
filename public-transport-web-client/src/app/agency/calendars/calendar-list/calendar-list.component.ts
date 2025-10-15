import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {CalendarQuery, CalendarService, GetCalendarsResponse, Status} from "../../../generated/public-transport-api";
import {size} from "lodash";
import {LoginService} from "../../../auth/login.service";

@Component({
    selector: 'app-calendars',
    templateUrl: './calendar-list.component.html',
    styleUrl: './calendar-list.component.scss'
})
export class CalendarListComponent implements OnInit {

    public calendarsResponse: GetCalendarsResponse;

    constructor(private calendarService: CalendarService, private loginService: LoginService, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.calendarsResponse = this.route.snapshot.data['calendars'];
    }

    public deleteByCalendarName(calendarName: string) {
        const query: CalendarQuery = {};
        query.calendarName = calendarName;
        this.calendarService.deleteCalendarByCalendarName(this.loginService.getInstance(), query).subscribe((response: Status) => {
            this.calendarService.getCalendars(this.loginService.getInstance())
                .subscribe((calendarResponse: GetCalendarsResponse) => this.calendarsResponse = calendarResponse);
        });
    }

    public hasElements(array: any[]): boolean {
        return array.length !== 0;
    }

    public hasCalendar(): boolean {
        return size(this.calendarsResponse.calendars) > 0;
    }
}
