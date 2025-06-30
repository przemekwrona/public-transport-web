import {Component} from '@angular/core';
import {Route, RouteService, Status, Stop} from "../../../generated/public-transport";
import {Router} from "@angular/router";
import {BusStopSelectorData} from "../../shared/bus-stop-selector/bus-stop-selector.component";
import {AuthService} from "../../../auth/auth.service";
import {NotificationService} from "../../../shared/notification.service";

@Component({
    selector: 'app-create-route',
    templateUrl: './create-route.component.html',
    styleUrl: './create-route.component.scss'
})
export class CreateRouteComponent {

    public route: Route = {
        google: false,
        active: true
    };

    public origin: BusStopSelectorData = {} as BusStopSelectorData;
    public destination: BusStopSelectorData = {} as BusStopSelectorData;

    constructor(private _router: Router, private routeService: RouteService, private authService: AuthService, private notificationService: NotificationService) {
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

        this.routeService.createRoute(this.authService.getInstance(), this.route).subscribe((status: Status) => {
            this.notificationService.showSuccess(`Linia ${this.route.line} (${this.route.name}) została pomyślnie utworzona`);
            this._router.navigate(['/agency/trips/create'], {
                queryParams: {
                    line: this.route.line,
                    name: this.route.name
                }
            }).then();
        });
    }
}
