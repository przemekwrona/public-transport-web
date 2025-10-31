import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Data, Params, Router, RouterModule} from "@angular/router";
import {
    Route, RouteDetails,
    RouteId, RouteService,
    Trip,
    TripId,
    TripService,
    UpdateRouteRequest
} from "../../../generated/public-transport-api";
import {faCircleXmark, IconDefinition} from '@fortawesome/free-solid-svg-icons';
import {remove} from "lodash";
import {map} from "rxjs";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {TranslocoPipe} from "@jsverse/transloco";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import moment from "moment";

@Component({
    standalone: true,
    selector: 'app-trip-list',
    templateUrl: './trip-list.component.html',
    styleUrl: './trip-list.component.scss',
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        FaIconComponent,
        TranslocoPipe
    ],
    providers: [
        TripService,
        RouteService
    ]
})
export class TripListComponent implements OnInit {

    @ViewChild('noVariants') noVariants!: ElementRef;
    @ViewChild('variants') variants!: ElementRef;


    public faCircleXmark: IconDefinition = faCircleXmark;

    public params: Params;
    public trips: RouteDetails = {route: {line: '', name: ''}};

    public state: { line: string, name: string, variant: string };

    constructor(private tripService: TripService, private agencyStorageService: AgencyStorageService, private routeService: RouteService, private _router: Router, private _route: ActivatedRoute) {
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
        const tripId: TripId = {line: trip.line, name: trip.name, variant: trip.variant, mode: trip.mode} as TripId;
        this.tripService.deleteTripByTripId(this.agencyStorageService.getInstance(), tripId).subscribe(response => {
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

        this.routeService.updateRoute(this.agencyStorageService.getInstance(), updateRouteRequest).subscribe((response) => {
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

    public isCreatedOrUpdated(trip: Trip): boolean {
        const twoMinutesAgo = moment().subtract(1, 'minute');
        return moment(trip.createdAt).isAfter(twoMinutesAgo)
            || moment(trip.updatedAt).isAfter(twoMinutesAgo)
    }

}
