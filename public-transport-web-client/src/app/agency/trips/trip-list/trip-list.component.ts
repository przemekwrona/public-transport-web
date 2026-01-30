import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Data, Params, Router, RouterModule} from "@angular/router";
import {
    Route, RouteDetails,
    RouteId, RouteService, Stop, Trip,
    TripId, TripMode,
    TripService,
    UpdateRouteRequest
} from "../../../generated/public-transport-api";
import {faCircleXmark, faMap, faSpinner, IconDefinition} from '@fortawesome/free-solid-svg-icons';
import {remove} from "lodash";
import {map} from "rxjs";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {TranslocoPipe} from "@jsverse/transloco";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {
    BusStopData,
    BusStopModalSelectorComponent, BusStopSelectorConfig, BusStopSelectorData
} from "../../shared/bus-stop-modal-selector/bus-stop-modal-selector.component";
import {MatDialog} from "@angular/material/dialog";
import {TripItemComponent} from "./trip-item/trip-item.component";

@Component({
    selector: 'app-trip-list',
    templateUrl: './trip-list.component.html',
    styleUrl: './trip-list.component.scss',
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        FaIconComponent,
        TranslocoPipe,
        TripItemComponent
    ],
    providers: [
        TripService,
        RouteService
    ]
})
export class TripListComponent implements OnInit {
    protected readonly faMap: IconDefinition = faMap;

    @ViewChild('noVariants') noVariants!: ElementRef;
    @ViewChild('variants') variants!: ElementRef;

    protected readonly faSpinner = faSpinner;

    public faCircleXmark: IconDefinition = faCircleXmark;

    public params: Params;
    public trips: RouteDetails = {route: {routeId: {line: '', name: ''}}};

    public state: { line: string, name: string, version: number };

    public isUpdatingBasicInformation: boolean = false;

    constructor(private tripService: TripService, private agencyStorageService: AgencyStorageService, private routeService: RouteService, private _router: Router, private _route: ActivatedRoute, private dialog: MatDialog) {
    }

    ngOnInit(): void {
        this._route.data.pipe(map((data: Data) => data['trips'])).subscribe(trips => this.trips = trips);
        this._route.queryParams.subscribe(params => this.state = params as {
            line: string,
            name: string,
            version: number
        });
    }

    public createTrip() {
        this._router.navigate(['/agency/trips/create'], {queryParams: this.state});
    }

    public hasVariants(): boolean {
        return (this.trips?.trips || []).length > 0;
    }

    public saveBasicInfo(): void {
        this.isUpdatingBasicInformation = true;

        const routeId: RouteId = {
            line: this.state.line,
            name: this.state.name,
            version: this.state.version
        }

        const route: Route = {
            routeId: {
                line: this.trips.route.routeId.line,
                name: this.trips.route.routeId.name,
                version: this.trips.route.routeId.version
            },
            google: this.trips.route.google,
            active: this.trips.route.active,
            description: this.trips.route.description
        }

        const updateRouteRequest: UpdateRouteRequest = {
            routeId: routeId,
            route: route
        }

        this.routeService.updateRoute(this.agencyStorageService.getInstance(), updateRouteRequest).subscribe({
            next: (response) => {
                this.state = {
                    line: route.routeId.line,
                    name: route.routeId.name,
                    version: route.routeId.version
                }
            },
            complete: () => this.isUpdatingBasicInformation = false
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

    public mapPreview(stop: Stop): void {
        const busStopData: BusStopData = {} as BusStopData;
        busStopData.stopId = stop.id;
        busStopData.stopName = stop.name;
        busStopData.stopLon = stop.lon;
        busStopData.stopLat = stop.lat;

        const config: BusStopSelectorConfig = {} as BusStopSelectorConfig;
        config.showTitle = false;
        config.fullScreen = true;
        config.scrollable = false;

        const busStopSelectorData: BusStopSelectorData = {} as BusStopSelectorData;
        busStopSelectorData.busStop = busStopData;
        busStopSelectorData.config = config;

        const dialogRef = this.dialog.open(BusStopModalSelectorComponent, {
            width: '90%',
            height: '70%',
            data: busStopSelectorData
        });
    }

    public onDelete($event: TripId): void {
        const agency: string = this.agencyStorageService.getInstance();
        this.tripService.deleteTripByTripId(agency, $event).subscribe({
            next: (response) => remove(this.trips.trips, {
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
}
