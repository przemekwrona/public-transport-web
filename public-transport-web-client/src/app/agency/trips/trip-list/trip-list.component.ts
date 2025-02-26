import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, ActivatedRouteSnapshot, Data, ParamMap, Params, Router} from "@angular/router";
import {Route, RouteId, Trip, Trips, UpdateRouteRequest} from "../../../generated/public-transport";
import {TripsService} from "../trips.service";
import {faCircleXmark, IconDefinition} from '@fortawesome/free-solid-svg-icons';
import {RoutesService} from "../../routes/routes.service";
import {remove} from "lodash";
import {map} from "rxjs";

@Component({
    selector: 'app-trip-list',
    templateUrl: './trip-list.component.html',
    styleUrl: './trip-list.component.scss'
})
export class TripListComponent implements OnInit {

    @ViewChild('noVariants') noVariants!: ElementRef;
    @ViewChild('variants') variants!: ElementRef;


    public faCircleXmark: IconDefinition = faCircleXmark;

    public params: Params;
    public trips: Trips = {route: {line: '', name: ''}};

    public state: { line: string, name: string, variant: string };

    constructor(private tripService: TripsService, private routeService: RoutesService, private _router: Router, private _route: ActivatedRoute) {
    }

    ngOnInit(): void {

        this._route.data.pipe(map((data: Data) => data['trips'])).subscribe(trips => this.trips = trips);
        this._route.queryParams.subscribe(params => this.state = params as {
            line: string,
            name: string,
            variant: string
        });
    }

    public createTrip() {
        this._router.navigate(['/agency/trips/create'], {queryParams: this.state});
    }

    public editTrip(trip: Trip) {
        const queryParams = {name: trip.name, line: trip.line, variant: trip.variant, mode: trip.mode};
        this._router.navigate(['/agency/trips/edit'], {queryParams: queryParams});
    }

    public deleteTrip(trip: Trip) {
        this.tripService.delete(trip.line, trip.name, trip.variant, trip.mode).subscribe(response => {
            remove(this.trips.trips, {line: trip.line, name: trip.name, variant: trip.variant, mode: trip.mode})
        });
    }

    public hasVariants(): boolean {
        return (this.trips?.trips || []).length > 0;
    }

    public saveBasicInfo(): void {
        const routeId: RouteId = {
            line: this.state.line,
            name: this.state.name
        }

        const route: Route = {
            name: this.trips.route.name,
            line: this.trips.route.line,
            google: this.trips.route.google,
            active: this.trips.route.active,
            description: this.trips.route.description
        }

        const updateRouteRequest: UpdateRouteRequest = {
            routeId: routeId,
            route: route
        }

        this.routeService.updateRoute(updateRouteRequest).subscribe((response) => {
        });
    }

    public scrollNoVariants(): void {
        if (this.noVariants) {
            this.noVariants.nativeElement.scrollIntoView({behavior: 'smooth'});
        }
    }

    public scrollVariants(): void {
        if (this.variants) {
            this.variants.nativeElement.scrollIntoView({behavior: 'smooth'});
        }
    }

}
