import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {TranslocoPipe} from '@jsverse/transloco';
import {
    AgencyStop,
    AgencyStopRoute,
    AgencyStopTrip,
    AgencyStops
} from '../../generated/public-transport-api';
import {GoogleAnalyticsService} from '../../google-analytics.service';

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

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private googleAnalyticsService: GoogleAnalyticsService
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
