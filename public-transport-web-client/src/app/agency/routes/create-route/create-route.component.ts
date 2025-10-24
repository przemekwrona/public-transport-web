import {Component, OnInit} from '@angular/core';
import {ModificationRouteResponse, Route, RouteService, Status, Stop} from "../../../generated/public-transport-api";
import {Router} from "@angular/router";
import {BusStopSelectorData} from "../../shared/bus-stop-selector/bus-stop-selector.component";
import {LoginService} from "../../../auth/login.service";
import {NotificationService} from "../../../shared/notification.service";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

@Component({
    selector: 'app-create-route',
    templateUrl: './create-route.component.html',
    styleUrl: './create-route.component.scss'
})
export class CreateRouteComponent implements OnInit {

    public route: Route = {
        google: false,
        active: true
    };

    public origin: BusStopSelectorData = {} as BusStopSelectorData;
    public destination: BusStopSelectorData = {} as BusStopSelectorData;

    constructor(private _router: Router, private routeService: RouteService, private agencyStorageService: AgencyStorageService, private notificationService: NotificationService) {
    }

    ngOnInit(): void {
        this.agencyStorageService.getAgencyAddress().subscribe(agencyAddress => {
            this.origin.stopLon = agencyAddress.lon;
            this.origin.stopLat = agencyAddress.lat;

            this.destination.stopLon = agencyAddress.lon;
            this.destination.stopLat = agencyAddress.lat;
        });
    }

    public createRouteAndNavigateToCreateNewTrip(): void {
        const originStop: Stop = {} as Stop;
        originStop.id = this.origin.stopId;
        originStop.name = this.origin.stopName

        const destinationStop: Stop = {} as Stop;
        destinationStop.id = this.destination.stopId;
        destinationStop.name = this.destination.stopName;

        this.route.originStop = originStop;
        this.route.destinationStop = destinationStop;

        this.routeService.createRoute(this.agencyStorageService.getInstance(), this.route).subscribe((response: ModificationRouteResponse) => {
            this.notificationService.showSuccess(`Linia ${this.route.line} (${this.route.name}) została pomyślnie utworzona`);
            this._router.navigate(['/agency/trips/create'], {
                queryParams: {
                    line: response.routeId.line,
                    name: response.routeId.name
                }
            }).then();
        });
    }


}
