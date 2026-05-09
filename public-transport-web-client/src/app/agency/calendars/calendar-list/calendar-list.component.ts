import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {CalendarQuery, CalendarService, GetCalendarsResponse, Status} from "../../../generated/public-transport-api";
import {size} from "lodash";
import {LoginService} from "../../../auth/login.service";
import {MatDialog} from "@angular/material/dialog";
import {CalendarItemModalComponent} from "../calendar-item-modal/calendar-item-modal.component";

@Component({
    selector: 'app-calendars',
    templateUrl: './calendar-list.component.html',
    styleUrl: './calendar-list.component.scss',
    standalone: false
})
export class CalendarListComponent implements OnInit {

    public calendarsResponse: GetCalendarsResponse;

    constructor(private calendarService: CalendarService, private loginService: LoginService, private route: ActivatedRoute, private dialog: MatDialog) {
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

    openDialog() {
        const dialogRef = this.dialog.open(CalendarItemModalComponent);

        dialogRef.afterClosed().subscribe(result => {
            console.log(`Dialog result: ${result}`);
        });
    }
}
