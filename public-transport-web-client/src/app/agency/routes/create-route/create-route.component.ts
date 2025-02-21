import {Component} from '@angular/core';
import {RoutesService} from "../routes.service";
import {Route, Status} from "../../../generated/public-transport";

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

    constructor(private routeService: RoutesService) {
    }

    public createRoute(): void {

        this.routeService.createRoute(this.route).subscribe((status: Status) => {

        });
    }


}
