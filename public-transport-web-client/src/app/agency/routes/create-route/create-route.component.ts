import {Component, ElementRef, Renderer2, ViewChild} from '@angular/core';
import {RoutesService} from "../routes.service";
import {Route, Status} from "../../../generated/public-transport";
import {Router} from "@angular/router";
import {BusStopSelectorData} from "../../shared/bus-stop-selector/bus-stop-selector.component";

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

    constructor(private routeService: RoutesService, private _router: Router) {
    }

    public createRoute(): void {
        this.routeService.createRoute(this.route).subscribe((status: Status) => {
            this._router.navigate(['/agency/trips/create'], {
                queryParams: {
                    line: this.route.line,
                    name: this.route.name
                }
            }).then();
        });
    }
}
