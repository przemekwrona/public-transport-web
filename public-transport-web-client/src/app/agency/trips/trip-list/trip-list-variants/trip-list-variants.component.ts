import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Data, Router} from "@angular/router";
import {
    RouteDetails,
    Trip,
    TripId1,
    TripMode,
    TripService
} from "../../../../generated/public-transport-api";
import {remove} from "lodash";
import {map} from "rxjs";
import {CommonModule} from "@angular/common";
import {TranslocoPipe} from "@jsverse/transloco";
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {TripItemComponent} from "../trip-item/trip-item.component";

@Component({
    selector: 'app-trip-list-variants',
    templateUrl: './trip-list-variants.component.html',
    imports: [
        CommonModule,
        TranslocoPipe,
        TripItemComponent
    ],
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
        this._route.data.pipe(map((data: Data) => data['routeDetails'])).subscribe(trips => {
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
            next: () => remove(this.trips.trips, {
                line: $event.routeId.line,
                name: $event.routeId.name,
                variant: $event.variantName,
                mode: $event.variantMode,
                trafficMode: $event.trafficMode
            })
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
