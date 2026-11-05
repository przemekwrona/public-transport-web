import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {
    AgencyStop,
    AgencyStopRoute,
    AgencyStopTimetableService,
    AgencyStopTrip,
    AgencyStops
} from '../../generated/public-transport-api';
import {LoginService} from '../../auth/login.service';
import {GoogleAnalyticsService} from '../../google-analytics.service';
import {NotificationService} from '../../shared/notification.service';
import {
    AgencyStopNameDialogData,
    AgencyStopNameDialogResult,
    AgencyStopNameModalComponent
} from './agency-stop-name-modal/agency-stop-name-modal.component';

@Component({
    selector: 'app-agency-stops',
    imports: [RouterLink, TranslocoPipe],
    templateUrl: './agency-stop-list.component.html',
    styleUrl: './agency-stop-list.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager
})
export class AgencyStopListComponent implements OnInit {

    public stops: AgencyStops = {items: []};
    public query = '';
    public downloadingStopId: number | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private googleAnalyticsService: GoogleAnalyticsService,
        private dialog: MatDialog,
        private loginService: LoginService,
        private agencyStopTimetableService: AgencyStopTimetableService,
        private notificationService: NotificationService,
        private translocoService: TranslocoService
    ) {
    }

    ngOnInit(): void {
        this.stops = this.route.snapshot.data['stops'] ?? {items: []};
        this.googleAnalyticsService.pageView(this.router.url);
    }

    public get visibleStops(): AgencyStop[] {
        const items = this.stops.items ?? [];
        const query = this.query.trim().toLowerCase();
        if (!query) {
            return items;
        }
        return items.filter(item => this.matches(item, query));
    }

    public hasStops(): boolean {
        return (this.stops.items ?? []).length > 0;
    }

    public tripsFor(item: AgencyStop, route: AgencyStopRoute): AgencyStopTrip[] {
        return (item.trips ?? []).filter(trip => trip.routeCode === route.routeCode);
    }

    public onSearch(event: Event): void {
        this.query = (event.target as HTMLInputElement).value;
    }

    public downloadTimetable(item: AgencyStop): void {
        const stopId = item.stop?.id;
        if (stopId == null || this.downloadingStopId != null) {
            return;
        }
        this.downloadingStopId = stopId;
        this.agencyStopTimetableService.downloadAgencyStopTimetable(
            this.loginService.getInstance(),
            stopId,
            'body',
            false,
            {httpHeaderAccept: 'application/pdf'}
        ).subscribe({
            next: response => {
                const blob = response instanceof Blob
                    ? response
                    : new Blob([response as BlobPart], {type: 'application/pdf'});
                const objectUrl = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = objectUrl;
                link.download = `rozklad-${this.fileName(item, stopId)}.pdf`;
                link.click();
                URL.revokeObjectURL(objectUrl);
                this.downloadingStopId = null;
            },
            error: () => {
                this.downloadingStopId = null;
                this.notificationService.showError(this.translocoService.translate('agency.stops.used.download.failed'));
            }
        });
    }

    public openNameEditor(item: AgencyStop): void {
        const stop = item.stop;
        if (stop?.id == null) {
            return;
        }
        const data: AgencyStopNameDialogData = {
            stopId: stop.id,
            name: stop.name ?? '',
            lat: stop.lat,
            lon: stop.lon
        };
        this.dialog.open(AgencyStopNameModalComponent, {
            data,
            width: '960px',
            maxWidth: '95vw'
        }).afterClosed().subscribe((result: AgencyStopNameDialogResult | undefined) => {
            if (result && item.stop && result.stopId === item.stop.id) {
                item.stop.name = result.name;
            }
        });
    }

    private fileName(item: AgencyStop, stopId: number): string {
        const name = item.stop?.name?.trim();
        const label = name ? name : `przystanek-${stopId}`;
        return label.replaceAll(/\s+/g, '_');
    }

    public trackStop(item: AgencyStop): number | string {
        return item.stop?.id ?? item.stop?.name ?? '';
    }

    private matches(item: AgencyStop, query: string): boolean {
        const stopName = item.stop?.name?.toLowerCase() ?? '';
        if (stopName.includes(query)) {
            return true;
        }
        return (item.routes ?? []).some(route =>
                (route.line ?? '').toLowerCase().includes(query)
                || (route.name ?? '').toLowerCase().includes(query))
            || (item.trips ?? []).some(trip =>
                (trip.tripCode ?? '').toLowerCase().includes(query)
                || (trip.headsign ?? '').toLowerCase().includes(query));
    }
}
