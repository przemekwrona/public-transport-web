import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {
    RouteDetails,
    Trip,
    TripId1,
    TripMode,
    TripService
} from "../../../../generated/public-transport-api";
import {remove} from "lodash";
import {routeDetailsData} from "../trip-list.resolver";

import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {TripItemComponent} from "../trip-item/trip-item.component";

@Component({
    selector: 'app-trip-list-variants',
    templateUrl: './trip-list-variants.component.html',
    imports: [
    TripItemComponent
],
    changeDetection: ChangeDetectionStrategy.Eager,
    providers: [
        TripService
    ]
})
export class TripListVariantsComponent implements OnInit {
    public trips: RouteDetails = {route: {routeId: {line: '', name: ''}}};
    public state: { line: string, name: string, version: number };
    public routeCode: string = '';

    constructor(
        private tripService: TripService,
        private agencyStorageService: AgencyStorageService,
        private _router: Router,
        private _route: ActivatedRoute
    ) {
    }

    ngOnInit(): void {
        routeDetailsData(this._route)?.subscribe(trips => {
            this.trips = trips;
            this.routeCode = trips.route.routeCode;
            this.state = {
                line: trips.route.routeId.line,
                name: trips.route.routeId.name,
                version: trips.route.routeId.version
            };
        });
        this._route.queryParams.subscribe(params => {
            if (params['line'] || params['name'] || params['version']) {
                this.state = params as { line: string, name: string, version: number };
            }
        });
    }

    public createTrip(tripMode: TripMode = TripMode.Front) {
        this._router.navigate(['/agency/routes', this.routeCode, 'trips', 'create'], {queryParams: {tripMode}}).then(() => {
        });
    }

    public hasVariants(): boolean {
        return (this.trips?.trips || []).length > 0;
    }

    public onDelete($event: TripId1): void {
        const agency: string = this.agencyStorageService.getInstance();
        this.tripService.deleteTripByTripId(agency, this.routeCode, $event.tripCode).subscribe({
            next: () => remove(this.trips.trips, (trip: Trip) => trip.tripId?.tripCode === $event.tripCode)
        });
    }

    public findTripsWithDirectionFront(): Trip[] {
        return this.trips.trips.filter(trip => trip.mode === TripMode.Front);
    }

    public findTripsWithDirectionBack(): Trip[] {
        return this.trips.trips.filter(trip => trip.mode === TripMode.Back);
    }

    protected readonly TripMode = TripMode;
}
