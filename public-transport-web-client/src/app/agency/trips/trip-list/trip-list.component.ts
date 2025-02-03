import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, ActivatedRouteSnapshot, Data, ParamMap, Params, Router} from "@angular/router";
import {Route, Trip, Trips} from "../../../generated/public-transport";
import {TripsService} from "../trips.service";

@Component({
    selector: 'app-trip-list',
    templateUrl: './trip-list.component.html',
    styleUrl: './trip-list.component.scss'
})
export class TripListComponent implements OnInit {

    public params: Params;
    public trips: Trips;

    public state: { name: string, line: string, variant: string };

    constructor(private tripService: TripsService, private _router: Router, private _route: ActivatedRoute) {
        const navigation = this._router.getCurrentNavigation();
        this.state = navigation?.extras.state as { name: string, line: string, variant: string };
    }

    ngOnInit(): void {
        this.tripService.getTrips(this.state.name, this.state.line).subscribe((trips: Trips) => {
            this.trips = trips;
        });
    }

    public hasVia(): boolean {
        return false;
        // return this.params['via'] !== undefined && this.params['via'].length > 0;
    }

    public createTrip() {
        // this._router.navigateByUrl('/agency/trips/create', {state: state});
        this._router.navigate(['/agency/trips/create'], {state: this.state});
    }

    public editTrip(trip: Trip) {
        const state = {name: trip.name, line: trip.line, variant: trip.variant, mode: trip.mode};
        this._router.navigateByUrl('/agency/trips/edit', {state: state});
    }

}
