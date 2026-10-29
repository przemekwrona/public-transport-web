import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
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
import moment from "moment";

@Component({
    selector: 'app-calendars',
    templateUrl: './calendar-list.component.html',
    styleUrl: './calendar-list.component.scss',
    standalone: false
})
export class CalendarListComponent implements OnInit, AfterViewInit, OnDestroy {

    public calendarsResponse: GetCalendarItemResponse;
    public selectedCalendarCode: string | null = null;
    public pulsingSymbol: { calendarCode: string; symbol: string } | null = null;
    private openedFromCreateCode: string | null = null;
    private pulseTimer: ReturnType<typeof setTimeout> | null = null;

    constructor(private calendarService: CalendarService, private loginService: LoginService, private route: ActivatedRoute, private router: Router, private dialog: MatDialog, private notificationService: NotificationService) {
        const navigation = this.router.getCurrentNavigation();
        const previousUrl = (navigation?.previousNavigation?.finalUrl ?? navigation?.previousNavigation?.extractedUrl)?.toString() ?? '';
        const createdSymbol = navigation?.extras?.state?.['createdSymbol'] as { calendarCode: string; symbol: string } | undefined;
        const cameFromCreate = /\/agency\/calendars\/[^/?#]+\/symbol\/create\/?(?:[?#].*)?$/.test(previousUrl);

        if (cameFromCreate && createdSymbol?.calendarCode && createdSymbol?.symbol) {
            this.pulsingSymbol = createdSymbol;
            this.openedFromCreateCode = createdSymbol.calendarCode;
        }
    }

    ngOnInit(): void {
        this.calendarsResponse = this.route.snapshot.data['calendars'];
        this.selectDefaultCalendar();
        if (this.pulsingSymbol) {
            this.pulseTimer = setTimeout(() => {
                this.pulsingSymbol = null;
            }, 5000);
        }
    }

    ngAfterViewInit(): void {
        if (!this.pulsingSymbol) {
            return;
        }
        setTimeout(() => {
            document.getElementById('created-calendar-symbol')?.scrollIntoView({behavior: 'smooth', block: 'center'});
        });
    }

    ngOnDestroy(): void {
        if (this.pulseTimer) {
            clearTimeout(this.pulseTimer);
        }
    }

    public deleteByCalendarSymbol(calendarSymbol: CalendarSymbolBody) {
        const calendarCode = calendarSymbol.calendarSymbolId.calendarItemId.code;
        const calendarSymbolCode = calendarSymbol.calendarSymbolId.symbol;

        this.calendarService.deleteCalendarByCalendarNameAndSymbol(this.loginService.getInstance(), calendarCode, calendarSymbolCode).subscribe((response: Status) => {
            this.calendarService.getCalendarItems(this.loginService.getInstance())
                .subscribe((calendarResponse: GetCalendarItemResponse) => this.setCalendars(calendarResponse));
        });
    }

    public selectCalendar(calendarItem: CalendarItemBody): void {
        this.selectedCalendarCode = calendarItem?.calendarItemId?.code ?? null;
    }

    public isCalendarSelected(calendarItem: CalendarItemBody): boolean {
        return calendarItem?.calendarItemId?.code === this.selectedCalendarCode;
    }

    public get selectedCalendar(): CalendarItemBody | undefined {
        return (this.calendarsResponse?.items ?? []).find(item => item?.calendarItemId?.code === this.selectedCalendarCode);
    }

    public hasElements(array: any[]): boolean {
        return array.length !== 0;
    }

    public dayTileClass(active?: boolean): string {
        return active
            ? 'inline-flex items-center justify-center min-w-7 rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-600 ring-1 ring-green-500/30 ring-inset mr-1'
            : 'inline-flex items-center justify-center min-w-7 rounded-md bg-gray-50 px-2 py-1 text-xs font-medium text-gray-600 ring-1 ring-gray-500/10 ring-inset mr-1';
    }

    public hasCalendar(): boolean {
        return size(this.calendarsResponse.items) > 0;
    }

    public isPanelExpanded(calendarItem: CalendarItemBody): boolean {
        return this.isActive(calendarItem?.startDate, calendarItem?.endDate)
            || calendarItem?.calendarItemId?.code === this.openedFromCreateCode;
    }

    public isPulsingSymbol(calendarSymbol: CalendarSymbolBody): boolean {
        if (!this.pulsingSymbol) {
            return false;
        }
        return calendarSymbol?.calendarSymbolId?.calendarItemId?.code === this.pulsingSymbol.calendarCode
            && calendarSymbol?.calendarSymbolId?.symbol === this.pulsingSymbol.symbol;
    }

    public isStartInFuture(startDate?: string): boolean {
        return this.daysUntilStart(startDate) > 0;
    }

    public isActive(startDate?: string, endDate?: string): boolean {
        if (!startDate || !endDate) {
            return false;
        }
        const today = moment().startOf('day');
        return today.isSameOrAfter(moment(startDate).startOf('day'))
            && today.isSameOrBefore(moment(endDate).startOf('day'));
    }

    public daysUntilStartText(startDate?: string): string {
        const days: number = this.daysUntilStart(startDate);
        return days === 1 ? '1 dzień' : `${days} dni`;
    }

    private selectDefaultCalendar(): void {
        const items = this.calendarsResponse?.items ?? [];
        const fromCreate = this.openedFromCreateCode
            ? items.find(item => item?.calendarItemId?.code === this.openedFromCreateCode)
            : undefined;
        const expanded = items.find(item => this.isPanelExpanded(item));
        const selected = fromCreate ?? expanded ?? items[0];
        this.selectedCalendarCode = selected?.calendarItemId?.code ?? null;
    }

    private setCalendars(response: GetCalendarItemResponse): void {
        this.calendarsResponse = response;
        const stillSelected = (response.items ?? []).some(item => item?.calendarItemId?.code === this.selectedCalendarCode);
        if (!stillSelected) {
            this.openedFromCreateCode = null;
            this.selectDefaultCalendar();
        }
    }

    private daysUntilStart(startDate?: string): number {
        if (!startDate) {
            return 0;
        }
        return moment(startDate).startOf('day').diff(moment().startOf('day'), 'days');
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
        calendarItemId.code = calendarItemBody.calendarItemId.code;

        const request: DeleteCalendarItemRequest = {} as DeleteCalendarItemRequest;
        request.calendarItemId = calendarItemId;

        const instance: string = this.loginService.getInstance();

        this.calendarService.deleteCalendarItem(instance, request).subscribe({
            next: (status) => {
                console.log(status);
                this.calendarService.getCalendarItems(this.loginService.getInstance())
                    .subscribe((calendarResponse: GetCalendarItemResponse) => this.setCalendars(calendarResponse));
            },
            error: (response: HttpErrorResponse): void => {
                const payload: ErrorResponse = response.error;
                this.notificationService.showError(`${payload.errorCode} Nie można usunąć kalendarza usuń symbole`);
            }
        })
    }
}
