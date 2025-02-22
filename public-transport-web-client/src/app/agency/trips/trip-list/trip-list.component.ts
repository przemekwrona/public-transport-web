import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, ActivatedRouteSnapshot, Data, ParamMap, Params, Router} from "@angular/router";
import {Route, RouteId, Trip, Trips, UpdateRouteRequest} from "../../../generated/public-transport";
import {TripsService} from "../trips.service";
import {faCircleXmark, faGlobe, IconDefinition} from '@fortawesome/free-solid-svg-icons';
import {RoutesService} from "../../routes/routes.service";

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
    public trips: Trips;

    public state: { name: string, line: string, variant: string };

    constructor(private tripService: TripsService, private routeService: RoutesService, private _router: Router, private _route: ActivatedRoute) {
        const navigation = this._router.getCurrentNavigation();
        this.state = navigation?.extras.state as { name: string, line: string, variant: string };
    }

    ngOnInit(): void {
        this.tripService.getTrips(this.state.name, this.state.line).subscribe((trips: Trips) => {
            this.trips = trips;
        });
    }

    public createTrip() {
        // this._router.navigateByUrl('/agency/trips/create', {state: state});
        this._router.navigate(['/agency/trips/create'], {state: this.state});
    }

    public editTrip(trip: Trip) {
        const state = {name: trip.name, line: trip.line, variant: trip.variant, mode: trip.mode};
        this._router.navigateByUrl('/agency/trips/edit', {state: state});
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
