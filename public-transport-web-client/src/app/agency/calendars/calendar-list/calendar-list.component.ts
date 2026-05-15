import {Component, inject, OnInit, signal, WritableSignal} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {
    CalendarSymbolQuery,
    CalendarService,
    CreateCalendarItemResponse, GetCalendarItemResponse,
    Status, CalendarSymbolBody, DeleteCalendarItemRequest, CalendarItemId, CalendarItemBody, ErrorResponse
} from "../../../generated/public-transport-api";
import {size} from "lodash";
import {LoginService} from "../../../auth/login.service";
import {MatDialog} from "@angular/material/dialog";
import {CalendarItemModalComponent} from "../calendar-item-modal/calendar-item-modal.component";
import {NotificationService} from "../../../shared/notification.service";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
    selector: 'app-calendars',
    templateUrl: './calendar-list.component.html',
    styleUrl: './calendar-list.component.scss',
    standalone: false
})
export class CalendarListComponent implements OnInit {

    public calendarsResponse: GetCalendarItemResponse;
    readonly panelOpenState: WritableSignal<boolean> = signal(false);

    constructor(private calendarService: CalendarService, private loginService: LoginService, private route: ActivatedRoute, private router: Router, private dialog: MatDialog, private notificationService: NotificationService) {
    }

    ngOnInit(): void {
        this.calendarsResponse = this.route.snapshot.data['calendars'];
    }

    public deleteByCalendarSymbol(calendarSymbol: CalendarSymbolBody) {
        const query: CalendarSymbolQuery = {};
        query.calendarName = calendarSymbol.calendarName;
        query.designation = calendarSymbol.designation;
        query.startDate = calendarSymbol.startDate;
        query.endDate = calendarSymbol.endDate;
        this.calendarService.deleteCalendarByCalendarNameAndSymbol(this.loginService.getInstance(), query).subscribe((response: Status) => {
            this.calendarService.getCalendarItems(this.loginService.getInstance())
                .subscribe((calendarResponse: GetCalendarItemResponse) => this.calendarsResponse = calendarResponse);
        });
    }

    public hasElements(array: any[]): boolean {
        return array.length !== 0;
    }

    public hasCalendar(): boolean {
        return size(this.calendarsResponse.items) > 0;
    }

    openDialog() {
        const dialogRef = this.dialog.open(CalendarItemModalComponent);

        dialogRef.afterClosed().subscribe((result: CreateCalendarItemResponse): void => {
            console.log(result);
            this.router.navigate(['/agency/calendars/create']).then((): void => {
            });
        });
    }

    public deleteItem(calendarItemBody: CalendarItemBody) {
        const calendarItemId: CalendarItemId = {} as CalendarItemId;
        calendarItemId.startDate = calendarItemBody.startDate;
        calendarItemId.endDate = calendarItemBody.endDate;

        const request: DeleteCalendarItemRequest = {} as DeleteCalendarItemRequest;
        request.calendarItemId = calendarItemId;

        const instance: string = this.loginService.getInstance();

        this.calendarService.deleteCalendarItem(instance, request).subscribe({
            next: (status) => {
                console.log(status);
                this.calendarService.getCalendarItems(this.loginService.getInstance())
                    .subscribe((calendarResponse: GetCalendarItemResponse) => this.calendarsResponse = calendarResponse);
            },
            error: (response: HttpErrorResponse): void => {
                const payload: ErrorResponse = response.error;
                this.notificationService.showError(`${payload.errorCode} Nie można usunąć kalendarza usuń symbole`);
            }
        })
    }
}
