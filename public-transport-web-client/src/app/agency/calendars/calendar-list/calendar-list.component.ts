import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {CalendarQuery, GetCalendarsResponse, Status} from "../../../generated/public-transport";
import {CalendarsService} from "../calendars.service";

@Component({
    selector: 'app-calendars',
    templateUrl: './calendar-list.component.html',
    styleUrl: './calendar-list.component.scss'
})
export class CalendarListComponent implements OnInit {

    public calendarsResponse: GetCalendarsResponse;

    constructor(private calendarService: CalendarsService, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.calendarsResponse = this.route.snapshot.data['calendars'];
    }

    public deleteByCalendarName(calendarName: string) {
        const query: CalendarQuery = {};
        query.calendarName = calendarName;
        this.calendarService.deleteCalendarByCalendarName(query).subscribe((response: Status) => {
            this.calendarService.getAllCalendars()
                .subscribe((calendarResponse: GetCalendarsResponse) => this.calendarsResponse = calendarResponse);
        });
    }
}
